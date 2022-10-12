/// <reference types="cypress" />

context('app', () => {
	// login before running each test
	beforeEach(() => {
	    const username = Cypress.env('username');
	    const password = Cypress.env('password');

		cy.request('/login')
		.its('body')
		.then((body) => {
		  // we can use Cypress.$ to parse the string body
		  // thus enabling us to query into it easily
		  const $html = Cypress.$(body)
		  const csrf = $html.find('input[name=_csrf]').val()
	
		  cy.request({
			method: 'POST',
			url: '/login',
			form: true,
			body: {
			  username: username,
			  password: password,
			  _csrf: csrf
			}
		  })
		  .then((resp) => {
			expect(resp.status).to.eq(200)
		  })
		});
	    
	})
})