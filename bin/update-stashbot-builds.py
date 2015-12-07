#!/usr/bin/env python

"""

Past a certain number of repos, it is unlikely that the internal "update all
builds" link in the stashbot will work; this is an external script that scrapes
the Stashbot admin UI to discover builds with Stashbot CI enabled, and hit the
"Save" button on the Stashbot CI Admin settings page with no modifications to
existing settings: this will force the regeneration of the Jenkins jobs on any
build which does not have "preserve jenkins settings" enabled.

This script requires two external python libraries; you can install them
via the standard "pip" tool:

    pip install -r requirements.txt

(It is recommended that you set up a python virtualenv for this, but
perhaps you are running in a container or a VM where you don't care;
in which case "sudo pip ...")

Given a working python env, this script takes a single argument: the hostname of
your stash/bitbucketserver instance.  It uses simple http auth (it is presumed
that your connection is secured via SSL) and you will be prompted for your
username and password: obviously you will only be able to update repos that
username can access, so it should probably be a user with administrative
privileges.

"""

# python stdlib imports
import getpass
import json
import sys
import time
from pprint import pprint as pp

# third party requirements: beautifulsoup4, requests
import requests
from bs4 import BeautifulSoup

# # uncomment for debuggery
#
# import logging
# import httplib
# httplib.HTTPConnection.debuglevel = 1
# logging.basicConfig()
# logging.getLogger().setLevel(logging.DEBUG)
# requests_log = logging.getLogger("requests.packages.urllib3")
# requests_log.setLevel(logging.DEBUG)
# requests_log.propagate = True

servername = sys.argv[1]

# fetch this many at a time
LIMIT = 100
# cut stash a break
SLEEP = 5

STASHAPI = 'https://%s/rest/api/1.0/repos?limit=%d' % (servername, LIMIT)
STASHBOT = 'https://%s/plugins/servlet/stashbot/repo-admin/' % servername

user = raw_input("username: ")
password = getpass.getpass(prompt="password: ")
auth = (user, password)
repos = []
is_last_page = False
start = 0

# use the stash REST API to fetch a list of all repositories
while is_last_page is False:
    print 'fetching %d repos starting at #%d ' % (LIMIT, start)
    r = requests.get(STASHAPI + '&start=%d' % start, auth=auth)
    answer = json.loads(r.text)
    repos.extend(answer['values'])
    is_last_page = answer['isLastPage']
    start += LIMIT
    print 'sleeping %d seconds so as not to thrash stash' % SLEEP
    time.sleep(SLEEP)

repo_urls = {}

# assemble the stashbot admin URL for each repo
for repo in repos:
    slug = repo['slug']
    project = repo['project']['key']
    if project.startswith('~'):
        continue
    url = STASHBOT + project + '/' + slug
    repo_urls[url] = {}

# scrape the stashbot admin page, find the current defaults for
# all form elements, assemble a large dict of form defaults.
#
# because stashbot does not have any sort of REST API, we use
# beautifulsoup4 to parse the HTML page and pull out all of the form
# values.  This is obviously potentially a little fragile. :(
SLEEP = 1
for url in repo_urls:
    status_page = requests.get(url, auth=auth).text
    soup = BeautifulSoup(status_page, 'html.parser')
    for input in soup.find_all('input', id='ciEnabled'):
        if input.get('checked') == 'checked':
            print '%s has stashbot ci enabled, adding to queue' % url

            text_inputs = soup.find_all(
                'input', type='text', class_='text', readonly=False)
            for ti in text_inputs:
                repo_urls[url][ti.get('id')] = ti.get('value')

            checkbox_inputs = soup.find_all('input', type='checkbox')
            for ci in checkbox_inputs:
                if ci.get('checked'):
                    repo_urls[url][ci.get('id')] = 'on'

            select_inputs = soup.find_all('select')
            for si in select_inputs:
                for option in si.find_all('option'):
                    if option.get('selected') == '':
                        repo_urls[url][si.get('id')] = option.get('value')
    print 'sleeping %d seconds so as not to thrash stash' % SLEEP
    time.sleep(SLEEP)

# filter out repos where there is nothing to do
update_urls = {}
for url in repo_urls:
    if repo_urls[url] != {}:
        update_urls[url] = repo_urls[url]

# let the user see what we're about to do
pp(update_urls)
doit = 'n'
while doit != 'y':
    doit = raw_input(
        'Does this look like a thing that you want to do? [yN] ').lower()
print 'okay...'

# post the form back to the admin page for each repo
SLEEP = 5
for url in update_urls:
    answer = requests.post(
        url,
        auth=auth,
        data=update_urls[url])
    print '%s: %s' % (url, answer.status_code)
    print 'sleeping %d seconds so as not to thrash jenkins' % SLEEP
    time.sleep(SLEEP)

print 'done!'
