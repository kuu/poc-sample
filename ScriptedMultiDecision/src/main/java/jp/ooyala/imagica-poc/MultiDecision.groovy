import tv.nativ.mio.api.plugin.command.PluginCommand
import groovy.json.JsonSlurper

class IdleAME extends PluginCommand {
  def execute() {

    def jsonSlurper = new JsonSlurper()

    context.logInfo("Querying AME1 server...")

    def result1 = makeCall "http://{AME1 host}/api/queue"
    def r1 = jsonSlurper.parseText(result1)

    if (r1.num == 0) {
      context.logInfo("AME1 is idle!")
      context.moveDate = new Date()
      return "AME1"
    }

    context.logInfo("Querying AME2 server...")

    def result2 = makeCall "http://{AME2 host}/api/queue"
    def r2 = jsonSlurper.parseText(result2)

    if (r2.num == 0) {
      context.logInfo("AME2 is idle!")
      context.moveDate = new Date()
      return "AME2"
    }

    context.logInfo("Querying AME3 server...")

    def result3 = makeCall "http://{AME3 host}/api/queue"
    def r3 = jsonSlurper.parseText(result3)

    if (r3.num == 0) {
      context.logInfo("AME3 is idle!")
      context.moveDate = new Date()
      return "AME3"
    }

    context.logInfo("No idle encoder.")

    return "Busy"
  }

  def makeCall(url,method = "GET", body = "",headers = [:]){

    def connection = new URL(url).openConnection();
    connection.setRequestMethod(method)
    headers.each(){ name,value ->
      connection.setRequestProperty(name, value)
    }
    if(method != "GET") {
      connection.setDoOutput(true)
      connection.getOutputStream().write(body.getBytes("UTF-8"));
    }
    def res = connection.getResponseCode();
    log("[" + method + "] " + url + " -> " + res);
    if((res < 200 || res > 299) && res != 404 || debug){

      log("[" + method + "] " + url + " -> " + res +
              "\nrequest body: " + body + "\nrequest headers: " + headers.inspect(),debug? 0 : 1)
    }
    //body = (res > 199 && res < 300) ? connection.getInputStream().getText() : null

    def resBody
    try {
      resBody = connection.getInputStream().getText()
      if(((res < 200 || res > 299) && res != 404) || debug){
        log("Response body: " + resBody?.toString(),debug? 0 : 1)
      }
    }catch(Exception e){
      //no body available! default to null
      log("No response body received. " + e.message,1)
    }


    return [code:res,body:resBody,headers:connection.getHeaderFields()]

  }

  def log(msg,priority=0){
    if(priority >= 2){
      throw new Exception(msg)
    }
    if(context){
      if(priority <= 0) {
        if (debug) context.logInfo(msg)
      }else{
        context.logWarning(msg)
      }
    }else{
      if(priority <= 0) {
        if (debug) println("INFO: " + msg)
      }else{
        println("WARN: " + msg)
      }
    }
  }

}