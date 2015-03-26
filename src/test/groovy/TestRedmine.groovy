import groovy.json.*

import org.junit.Test


class TestRedmine {

    @Test
    public void test() {

	/**
	 * Creating random id
	 */
	def random = new Random()
	def randomInt = random.nextInt(200-100+1)+100

	/**
	 * Redmine Macro information.
	 */
	def REDMINE_XWIKI_NAME = "xwiki"
	def REDMINE_CUSTOM_OBJECT = ""
	def REDMINE_AUTH_KEY = ""
	def REDMINE_DEFAULT_URL = "http://redmine.org"
	def REDMINE_MACRO_PARAMS = "1000"
	def REDMINE_CLOSE_STATUS_VALUE = "5"
	def REDMINE_QUERY = ""

	/**
	 *Fetch the information from Xwiki.
	 */
	REDMINE_CUSTOM_OBJECT = xwiki.getDocument("$REDMINE_XWIKI_NAME:RedmineMacro.RedmineMacroConfiguration").getObject("$REDMINE_XWIKI_NAME:RedmineMacro.RedmineMacroConfigurationClass")
	REDMINE_XWIKI_NAME = doc.getDocument().getWikiName()

	if(null == REDMINE_CUSTOM_OBJECT){
	    REDMINE_XWIKI_NAME = "xwiki"
	    REDMINE_CUSTOM_OBJECT = xwiki.getDocument("$REDMINE_XWIKI_NAME:RedmineMacro.RedmineMacroConfiguration").getObject("$REDMINE_XWIKI_NAME:RedmineMacro.RedmineMacroConfigurationClass")
	}

	REDMINE_CLOSE_STATUS_VALUE = "5"
	REDMINE_DEFAULT_URL = REDMINE_CUSTOM_OBJECT.getProperty("redmineUrl").value
	REDMINE_AUTH_KEY = "key=".concat(REDMINE_CUSTOM_OBJECT.getProperty("redmineUserApiKey").value)
	REDMINE_MACRO_PARAMS = "$xcontext.macro.params.params".replaceAll('issues', 'issues.json')

	def tableSortableCfg = "(% class=\"sortable filterable doOddEven\" id=\"redmineMacro-${randomInt}\" %)"
	def tableHeader = "(% class=\"sortHeader\" %)|= ID |= Tracker |= Status |= Thema "

	def issueUrls = [] as ArrayList
	def issueList = [] as ArrayList

	try {

	    if(REDMINE_MACRO_PARAMS.contains("?") && REDMINE_MACRO_PARAMS.contains("http")){
		REDMINE_QUERY = REDMINE_MACRO_PARAMS.concat("&").concat(REDMINE_AUTH_KEY)
		def root = new JsonSlurper().parseText(REDMINE_QUERY.toURL().text)
		root.issues.each() { issue ->
		    issueList.add(issue)
		}
		println tableSortableCfg
	    }else{
		REDMINE_MACRO_PARAMS = REDMINE_MACRO_PARAMS.replaceAll(" ", "")
		for(issueNumber in REDMINE_MACRO_PARAMS.split(",")) {
		    def issueUrl = REDMINE_DEFAULT_URL.concat("/issues/".concat(issueNumber).concat(".json").concat("?").concat(REDMINE_AUTH_KEY))
		    def root = new JsonSlurper().parseText(issueUrl.toURL().text)
		    issueList.add(root.issue)
		}
	    }

	    println tableHeader
	    for(issue in issueList) {
		assert ${issue.id} == 1000
		if(REDMINE_CLOSE_STATUS_VALUE.equalsIgnoreCase("${issue.status.id}")) {
		    println "| --[[#${issue.id}>>url:$REDMINE_DEFAULT_URL/issues/${issue.id}|]]-- | --{{{ ${issue.tracker.name} }}}-- | --{{{ ${issue.status.name} }}}-- | --{{{ ${issue.subject} }}}--"
		}else {
		    println "| [[#${issue.id}>>url:$REDMINE_DEFAULT_URL/issues/${issue.id}|]] | {{{ ${issue.tracker.name} }}} | {{{ ${issue.status.name} }}} | {{{ ${issue.subject} }}}"
		}
	    }
	} catch( Exception e ){
	    println "Something went wrong. Please contact the developer."
	    println "{{error}} {{{ $e }}} {{/error}}"
	}
    }
}
