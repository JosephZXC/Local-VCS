# Gitlet Design Document

**Name**: Jihao

## Classes and Data Structures

### Class 1: ***Commit***
#### Fields

1. Field 1: (String) parent
2. Field 2: (String) timestamp
3. Field 3: (String) message
4. Field 4: (TreeMap<String, String>) Blobs, which maps filename to sha1 of the file


### Class 2

#### Fields

1. Field 1
2. Field 2


## Algorithms

## Persistence

- .gitlet directory
    - objects directory
        - commits directory
          - (A serialized HashMap mapping commit sha1 to bytes[] contents,
            contents include meta info and tracked files of the commit, sha1 is calculated based on the bytes[] content)
        - blobs directory
          - (A serialized HashMap mapping blob sha1 to bytes[] contents, contents are the serialized bytes, sha1 is calculated
            based on the bytes[] content)
    - index directory
        - addition file that stores a serialized treemap for files staged for addition, 
            the map is a mapping from
          filename to sha1, which is calculated based on the serialized bytes[] content
        - removal file that stores a serialized treemap for files staged for removal,
          the map is a mapping from
          filename to null
    - HEAD file that has "head" as its filename and commit id
    - refs directory
      - A serialized HashMap that maps commit sha1 to branch name
