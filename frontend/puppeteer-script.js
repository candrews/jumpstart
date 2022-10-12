/**
 * @param {puppeteer.Browser} browser
 * @param {{url: string, options: LHCI.CollectCommand.Options}} context
 */
 module.exports = async (browser, context) => {
  const page = await browser.newPage();
  await page.goto(context.url);
  await page.type('input[name=username]', 'user');
  await page.type('input[name=password]', 'password');
  await page.keyboard.press('Enter');
  await page.waitForNavigation();
  await page.close();
};
