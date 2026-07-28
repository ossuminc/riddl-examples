#!/usr/bin/env bash
#
# Validate every example in this corpus against the RIDDL compiler and report
# message counts by kind.
#
# This deliberately does NOT go through the per-example .conf files: their
# `common` blocks set hide-warnings / hide-missing-warnings, which would
# suppress exactly the messages we are driving to zero. Flags are passed
# explicitly here instead.
#
# Usage:
#   bin/validate-corpus.sh            # summary table
#   bin/validate-corpus.sh -v         # summary table + full messages
#   bin/validate-corpus.sh ToDoodles  # one example only
#
# Exit status: 0 if every in-scope example has zero of the four goal message
# kinds (error, deprecated, missing, completeness); 1 otherwise.

set -uo pipefail

RIDDLC="${RIDDLC:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)/bin/riddlc}"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Examples excluded from the zero goal, with the reason.
# FooBarSameDomain deliberately defines the same type name in two included
# files so the ambiguity detector has something to catch. RIDDL 2.0 promoted
# that ambiguity from a warning to an error, so it can be a negative fixture
# or it can be clean, but not both. It stays a negative fixture.
EXCLUDED="FooBarSameDomain"

# entry points: <name>:<root riddl file relative to repo root>
ENTRIES=(
  "dokn:src/riddl/dokn/dokn.riddl"
  "FooBarSameDomain:src/riddl/FooBarSameDomain/FooBar.riddl"
  "FooBarSuccess:src/riddl/FooBarSuccess/FooBar.riddl"
  "FooBarTwoDomains:src/riddl/FooBarTwoDomains/FooBar.riddl"
  "ReactiveBBQ:src/riddl/ReactiveBBQ/ReactiveBBQ.riddl"
  "ReactiveSummit:src/riddl/ReactiveSummit/ReactiveSummit.riddl"
  "ShopifyCart:src/riddl/ShopifyCart/shopify-cart.riddl"
  "ToDoodles:src/riddl/ToDoodles/ToDoodles.riddl"
  "Trello:src/riddl/Trello/trello-riddl-model.riddl"
)

VERBOSE=0
FILTER=""
for arg in "$@"; do
  case "$arg" in
    -v|--verbose) VERBOSE=1 ;;
    *)            FILTER="$arg" ;;
  esac
done

if [[ ! -x "$RIDDLC" ]]; then
  echo "riddlc not found or not executable: $RIDDLC" >&2
  echo "Set RIDDLC to override." >&2
  exit 2
fi

OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT

printf '%s\n' "riddlc: $($RIDDLC -a version 2>&1 | tail -1 | sed 's/^\[info\] //')"
printf '\n'
printf '%-18s %7s %7s %7s %7s %7s %7s %s\n' \
  EXAMPLE ERROR DEPREC MISSING COMPLETE STYLE USAGE ""
printf '%s\n' "----------------------------------------------------------------------------------"

failed=0
for entry in "${ENTRIES[@]}"; do
  name="${entry%%:*}"
  file="${entry#*:}"
  [[ -n "$FILTER" && "$name" != "$FILTER" ]] && continue

  log="$OUT/$name.txt"
  # -a  no ANSI codes (parseable)
  # -G  group messages by kind (gives us the "<Kind> Message Count: N" lines)
  # -P  include remediation tips
  # the -w/-m/-s/-u/-c flags force every warning class to be shown
  "$RIDDLC" -a -G=true -P \
    -w=true -m=true -s=true -u=true -c=true \
    validate "$ROOT/$file" > "$log" 2>&1

  # grep -c prints 0 and exits 1 when there are no matches; the count is still
  # on stdout, so do not try to "correct" the exit status here.
  count() { local n; n=$(grep -c "^\[$1\]" "$log" 2>/dev/null); echo "${n:-0}"; }
  # subtract 1 for the "<Kind> Message Count: N" header line the grouping adds
  adj() { local n; n=$(count "$1"); if (( n > 0 )); then echo $(( n - 1 )); else echo 0; fi; }

  e=$(adj error); d=$(adj deprecated); m=$(adj missing)
  c=$(adj completeness); s=$(adj style); u=$(adj usage)

  note=""
  if [[ " $EXCLUDED " == *" $name "* ]]; then
    note="(negative fixture - excluded)"
  elif (( e + d + m + c > 0 )); then
    failed=1
  fi

  printf '%-18s %7s %7s %7s %7s %7s %7s %s\n' \
    "$name" "$e" "$d" "$m" "$c" "$s" "$u" "$note"

  if (( VERBOSE )); then
    sed 's/^/    /' "$log"
    printf '\n'
  fi
done

printf '\n'
if (( failed )); then
  echo "RESULT: not yet clean - goal kinds (error/deprec/missing/complete) remain."
  echo "Re-run with -v, or: $RIDDLC -a -G=true -P validate <root>.riddl"
else
  echo "RESULT: all in-scope examples clean on error/deprec/missing/complete."
  echo "(style and usage are out of scope and reported only)"
fi
exit $failed
