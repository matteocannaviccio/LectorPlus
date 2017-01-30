package it.uniroma3.lectorplus;

import it.uniroma3.lectorplus.WikiClean.WikiLanguage;

public class WikiCleanBuilder {
    private boolean withTitle = false;
    private boolean withFooter = false;
    private WikiLanguage lang = WikiLanguage.EN;
    private int ALIAS_SPAN = 200;

    public WikiCleanBuilder() {}

    public WikiCleanBuilder withTitle(boolean flag) {
	this.withTitle = flag;
	return this;
    }

    public WikiCleanBuilder withFooter(boolean flag) {
	this.withFooter = flag;
	return this;
    }

    public WikiCleanBuilder withLanguage(WikiLanguage lang) {
	this.lang = lang;
	return this;
    }
    
    public WikiCleanBuilder withAliasSpan(int span) {
   	this.ALIAS_SPAN = span;
   	return this;
       }

    public WikiClean build() {
	WikiClean clean = new WikiClean();
	clean.setWithTitle(withTitle);
	clean.setWithFooter(withFooter);
	clean.setLanguage(lang);
	clean.setALIAS_SPAN(ALIAS_SPAN);
	return clean;
    }
}
