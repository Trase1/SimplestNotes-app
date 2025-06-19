# How to contribute

If you want to help with the development of bundletool, please follow the following rules.

## Reporting bugs and feature requests

- Check if the issue has already been [reported](https://github.com/Ribeiro-Tiago/bundletool/issues). (including closed tickets).
- Create a ticket for your issue.
- Describe the issue clearly and succinctly.
- In case of bug reports:
  - describe the steps required to reproduce issue,
  - attach a [stacktrace](http://en.wikipedia.org/wiki/Stack_trace) if you have any,
  - post details about your setup:
    - application version,
    - system version
    - platform you were running and it's specifications

## New features or fixes

- Fork the repository on GitHub.
- Create your feature branch (check next step for naming rules)
- Commit your changes
- Update the [CHANGELOG](https://github.com/Ribeiro-Tiago/bundletool/master/CHANGELOG) to reflect your change
- Write tests
- Push to the branch
- Create new Pull Request that merges to dev branch,
  - If it's to solve an issue
    - Describe what the issue was and how your PR resolves it
    - link it to the open issue
  - If it's a feature / chore
    - Describe what you want to implment or what chore you're doing
- Patiently wait for the review.

#### Branch naming rules

Branch name must be prefix/name, where "name" is something succint that descrbies the purpose (e.g.: fix/issue-# or feat/foo) and prefix which is one of:

- fix > your branch fixes some issue
- feat > your branch implements a new feature
- chore > something else

## Coding rules

- Use prettier with the configurations included in the project
- Type everything you add
- Use **const** for every variable whose value doesn't change (array items and object properties don't count), otherwise use **let**
- use yarn
