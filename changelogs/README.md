# Creating the changelog

## How to create a changelog

- Take a look at all commits since the last release
- Group them into categories
- Create a new file in the `changelogs` folder with the name `x.y.z.md` where `x.y.z` is the version number
- Search all PRs and commits for the version number and add them to the changelog
- Filter for github: `is:pr is:closed closed:>2022-09-19 -author:app/dependabot`
- Credit all contributors in CONTRIBUTORS.md
- Make sure all issues are linked and closed
