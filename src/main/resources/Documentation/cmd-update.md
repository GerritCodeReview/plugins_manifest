@PLUGIN@ udpate
===============

NAME
----
@PLUGIN@ udpate - create/update a manifest

SYNOPSIS
--------
```
ssh -p @SSH_PORT@ @SSH_HOST@ @PLUGIN@ update
  [--src-project PROJECT]
  [--src-branch BRANCH]
  [--src-file FILE]
  [--dst-project PROJECT]
  [--dst-branch BRANCH]
  [--dst-file FILE]
  [--si SOFTWARE-IMAGE]
  [--project-revision PROJECT REVISION]
  [--create-change]
```

DESCRIPTION
-----------
Update or Create a manifest.

OPTIONS
-----------
**\-\-src-project PROJECT**

: Source PROJECT containing the manifest (default is "platform/manifest").

**\-\-src-branch BRANCH**

: Source BRANCH containing the manifest (default is "master").

**\-\-src-file FILE**

: Source FILE containing the manifest (default is "default.xml").

**\-\-dst-branch BRANCH**

: Destination BRANCH to place the manifest on.

**\-\-dst-file FILE**

: Destination FILE to place the manifest in.

**\-\-dst-project PROJECT**

: Destination PROJECT to place the manifest in.

**\-\-project-revision PROJECT REVISION**

: Name of the PROJECT in the manifest to update and the REVISION to update it to.

**\-\-commit-message COMMIT-MESSAGE**

: COMMIT-MESSAGE to be used on the manifest update commit

**\-\-create-change**

: A manifest change will be uploaded for review

Note: To help simplify specifying redundant parameters, the source
and destination parameters are tied to each other.  If either one
is specified, and the other is not, then the parameters will be
equal.

The default values are only used if neither the source nor the
destination is specified.

ACCESS
------
Any user who has configured an SSH key.

SCRIPTING
---------
This command is intended to be used in scripts.

EXAMPLES
--------
1) Update the default manifest to a tag for kernel/msm:

```
    $ ssh -p 29418 review.example.com @PLUGIN@ update
                   --project-revision kernel/msm refs/tags/mytag
    Commit: 35a6fe20991288e94902221bcca9474d51500484
```

2) Update the project revision for the kernel/msm project on the LA.UM.5.7 SI:

```
    $ ssh -p 29418 review.example.com @PLUGIN@ update
                   --si LA.UM.5.7
                   --project-revision kernel/msm refs/heads/master
    Commit: 201051e78dd1184e7cb1437f4196e53f75fe5315
```

3) Upload change to the manifest that updates revision of kernel/msm:

```
    $ ssh -p 29418 review.example.com @PLUGIN@ update
                   --project-revision kernel/msm master
                   --create-change
    Change Number: 1682219
```

4) Upload change to the manifest that updates multiple revisions:

```
    $ ssh -p 29418 review.example.com @PLUGIN@ update
                   --project-revision kernel/msm master
                   --project-revision kernel/lk master
                   --create-change
    Change Number: 1682220
```
