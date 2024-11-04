@allure.label.owner:baev
Feature: Labels

  @smoke
  Scenario Outline: Test name feature
    When I create name "<fullName>" via api
    Then I should see new name "<newName>" via api

    Examples:
      | fullName        | newName   |
      | vincent20181030 | vincent1  |
      | vincent20000000 | vincent2  |

  @smoke
  Scenario: Create new label via api
    When I create label with title "hello" via api
    Then I should see label with title "hello" via api

  @regress
  Scenario: Delete existing label via api
    When I create label with title "hello" via api
    And I delete label with title "hello" via api
    Then I should not see label with content "hello" via api
