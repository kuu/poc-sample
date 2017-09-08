import tv.nativ.mio.api.plugin.command.PluginCommand
import groovy.json.JsonSlurper

class AMEStatus extends PluginCommand {
  def execute() {

    def jsonSlurper = new JsonSlurper()

    context.logInfo("Querying AME1 server...")

    def statusText = makeCall "http://{AME1 host}/api/encoder"
    def status = jsonSlurper.parseText(statusText)

    return checkStatus(status.last, status.prev)
  }

  def checkStatus(last, prev) {
    if (last.state == "success") {
      if (last.date >= context.moveDate) {
        context.result = 'success'
        context.logInfo("Encoding success!")
        return true
      }
      return false;
    }

    if (last.state == "failed") {
      if (last.date >= context.moveDate) {
        context.result = 'failed'
        context.logInfo("Encoding failed.")
        return true
      }
      return false;
    }

    if (status.state == "started" && prev) {
      return checkStatus(prev, null)
    }
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