/*
 * Copyright 2016 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function addExampleButtons(){
$("#resource_operations .operation-params").find("td:eq(2)").append("<input type='button' value='Example JSON' onclick='if(loadExample){loadExample(this)}'>");
}

function loadExample(exampleButton){
    var urlSuffix = $(exampleButton).closest('.operation').find(".path").text().trim();
    var method = $(exampleButton).closest('.operation').find(".http_method").text().trim();

    var exampleUrl = "latest/example" + urlSuffix;

    if (method != 'post') {
        exampleUrl = exampleUrl + '/' + method
    }

    var onSuccess = function(response){
        var json=JSON.stringify(response, null,"   ");
        $(exampleButton.parentElement.parentElement).find("textarea").val(json);
    };
    $.ajax({url: exampleUrl, success: onSuccess});
}

function log() {
    if ('console' in window) {
      console.log.apply(console, arguments);
    }
}

function init(onSwaggerComplete){
    log('init')
    window.onload = function() {

      // Build a system
      const ui = SwaggerUIBundle({
        url: "latest/swagger.json",
        dom_id: '#swagger-ui',
        presets: [
          SwaggerUIBundle.presets.apis,
          SwaggerUIStandalonePreset
        ],
        plugins: [
          SwaggerUIBundle.plugins.DownloadUrl
        ],
        layout: "StandaloneLayout"
      })

      window.ui = ui
    }

//window.onload = function() {
//
//        // Build a system
//        const ui = SwaggerUIBundle({
//        url: "latest/swagger.json",
//        dom_id: '#swagger-ui',
//        presets: [
//        SwaggerUIBundle.presets.apis,
//        SwaggerUIStandalonePreset
//        ],
//        plugins: [
//        SwaggerUIBundle.plugins.DownloadUrl
//        ],
//        layout: "StandaloneLayout"
//        ,onComplete: function(swaggerApi, swaggerUi){
//        log("Loaded swagger");
//        $('pre code').each(function(i,e){hljs.highlightBlock(e)});
//        addExampleButtons();
//        if(onSwaggerComplete) {
//        onSwaggerComplete();
//        }
//        },
//        onFailure: function(data) {
//        log("Unable to Load SwaggerUI");
//        }})
//
//        window.ui = ui
}
