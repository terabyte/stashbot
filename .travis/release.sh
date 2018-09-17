#!/usr/bin/env bash
set -o errexit -o nounset

USERNAME="Dwolla Bot"
DEFAULT_BRANCH="master"

commit_username=$(git log -n1 --format=format:"%an")
if [[ "$commit_username" == "$USERNAME" ]]; then
  echo "Refusing to release a commit created by this script."
  exit 0
fi

if [ "$TRAVIS_BRANCH" != "$DEFAULT_BRANCH" ]; then
  echo "Only the $DEFAULT_BRANCH branch will be released. This branch is $TRAVIS_BRANCH."
  exit 0
fi

cp .travis/settings.xml "$HOME/.m2/settings.xml"
git config user.name "$USERNAME"
git config user.email "dev+dwolla-bot@dwolla.com"

git fetch origin ${DEFAULT_BRANCH}:refs/remotes/origin/${DEFAULT_BRANCH}
git remote set-head origin "$TRAVIS_BRANCH"
git show-ref --head
git symbolic-ref HEAD "refs/heads/$TRAVIS_BRANCH"
git symbolic-ref HEAD

DEFAULT_COMMIT=$(git rev-parse origin/${DEFAULT_BRANCH})
if [ "$TRAVIS_COMMIT" != "$DEFAULT_COMMIT" ]; then
  echo "Fetching $DEFAULT_BRANCH set HEAD to $DEFAULT_COMMIT, but Travis was building $TRAVIS_COMMIT, so refusing to continue."
  exit 0
fi

./bin/invoke-sdk.sh -B release:prepare release:perform
