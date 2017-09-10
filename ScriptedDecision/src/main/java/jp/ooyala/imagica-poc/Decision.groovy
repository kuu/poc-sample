import tv.nativ.mio.api.plugin.command.PluginCommand
import groovy.json.JsonSlurper

class Move extends PluginCommand {
  def debug = true
  def execute() {
    def targetEncoder = context.getStringVariable("targetEncoder")
    def fileName = context.getStringVariable("externalFileName")
    log("Querying " + targetEncoder)

    def result = makeCall "http://" + targetEncoder + "/api/encode/" + fileName
    if (result.code == 200) {
      context.setStringVariable("encodingStartedAt", (new Date()).toString())
      return true
    }
    context.setStringVariable("encodingResult", "failed")
    context.setStringVariable("encodingStartedAt", "-")
    context.setStringVariable("encodingCompletedAt", "-")
    return false
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