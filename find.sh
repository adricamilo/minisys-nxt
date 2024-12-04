#!/bin/bash

# Search string
SEARCH="dlcdn.apache.org"

echo "Searching for files containing: $SEARCH"
echo "-----------------------------------"

# Find all files recursively and search for the string
find . -type f -exec grep -l "$SEARCH" {} \; | while read -r file; do
    echo "Found in: $file"
    echo "Context:"
    echo "--------"
    # Display 3 lines before and after the match for context
    grep -A 3 -B 3 "$SEARCH" "$file"
    echo -e "\n"
done

# Check if we found anything
if [ $? -ne 0 ]; then
    echo "No files found containing the specified version."
fi

