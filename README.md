# SysNatTesting

SysNatTesting (or in short: SysNat or SNT) stands for a BDD testing tool to perform system tests in natural language. 

* * *

Its main features are

- It let you formulate instructions in natural language that operates your application under test and asserts the corresponding expectations.
- It creates a test report with a test result overview as well as detailed information about any instruction, expectations and results - all presented in natural language.

With these main features, SysNatTesting is perfectly suitable to bring domain experts and developers together in order to build a Domain Specific Language (DSL) in natural language.
Once established, this DSL allows domain experts to modify tests, to create new tests and to execute them without any support by developers because the natural language is intuitively understandable.
This tool is supposed to support cross functional teams and teams that follow DDD (Domain Driven Design).

Minor features of the SysNatTesting are:

- DSL Scripting (allows creation of natural language scripts that can be reused for different test cases - calling scripts in other scripts is possible as well)
- Easy import of test data (".dat" files for data structured as key-value pairs or Excel-data for data structured as tables)
- Test Parametrization (write one test and execute it many times with different datasets)
- Internationalization (any language can be used, see English and German examples below)
- Support of both declarative style (using BDD Keywords as with e.g. Gherkin), imperative style and the combination of both.

Technical Requirements:

- Java 8
- JUnit 4


* * *


Copyright by [IKS GmbH](https://www.iks-gmbh.com)

Licenced under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

Current version: **0.0.1**


* * *


Versioning convention: major.minor.revision

major:    will change for basic framework modification

minor:    will change for new features and larger improvements

revision: will change for bug fixes and smaller improvements


* * *


####Markdown Documentation

you can find documentation around markdown here:
- [Daring Fireball] [1]
- [Wikipedia - markdown] [2]

  [1]: http://daringfireball.net/projects/markdown/syntax
  [2]: http://en.wikipedia.org/wiki/Markdown
