describe('Login page', () => {
  it('successfully loads', () => {
    cy.visit('/login')
  })
  it('sets auth cookie when logging in via form submission', function () {
    const username = Cypress.env('username');
    const password = Cypress.env('password');

    cy.visit('/login')

    cy.get('input[name=username]').type(username)

    // {enter} causes the form to submit
    cy.get('input[name=password]').type(`${password}{enter}`)

    // we should be redirected to /
    cy.location('pathname').should('eq', '/');

    // our auth cookie should be present
    cy.getCookie('SESSION').should('exist')
  })
})