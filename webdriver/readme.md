This *webdriver* directory is supposed to contain all external stuff needed to launch a web browser. For Firefox 45 and higher this is the geckodriver. For different versions of firefox different versions of geckodrivers have to be used. By default, *SysNat* tries to find the Firefox installed in the default installation directory of which is "C://Program Files//Mozilla Firefox". You may change the SysNat default in the *execution.properties* to a different Firefox location.

If you wish to use a different (older or younger) Firefox version than installed on your computer, then download your favorite version from https://mozilla-firefox.de.uptodown.com/windows/versions and install it into this webdriver directory. Furthermore, set the corresponding path to it in the *execution.properties* and the correct geckodriver version (trial and error let you find it).

If you are looking for a newer or older WebDriver versions have a look here: 
- Geckodriver (for Firefox):  https://github.com/mozilla/geckodriver/releases
- Chrome: https://chromedriver.storage.googleapis.com/index.html
- IE: https://www.seleniumhq.org/download/

SysNat currently uses the Selenium version 3.13.0. Its update requires a source code change.

The experience has been made that for automated tests of web applications Firefox appears to be the most stable web browser - in general but especially when downloading files. Since your default web browser updates regularily, you should do the same for web driver used in the tests in order to maximise stability.