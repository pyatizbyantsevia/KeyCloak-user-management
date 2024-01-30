def getCodeAndBody(jsonResponse) {

    def codeMatcher = jsonResponse =~ /HTTP\/\d (\d+)/ 
    def code = codeMatcher ? codeMatcher[0][1] : null
    
    def bodyMatcher = jsonResponse =~ /\{([^}]*)\}/
    def body = bodyMatcher ? bodyMatcher[0][0] : null

    return ['code': code, 'body': body]
}

return this
