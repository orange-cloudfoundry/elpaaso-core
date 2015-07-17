/*
 * Copyright (C) 2015 Orange
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*********************************************************
*	 		ACCORDEON
* Auteur : Nicolas Mélix
* Date : 26/08/2010
* Code JSON
**********************************************************/

function StackPanel(id, multi_open) {
	var hideClass = "hide";
	var activeClass = "accordeon_zone_actif";
	var itemClass = "accordeon_zone";
	var linkClass = "accordeon_zone_item";
	var containerId = id;
	var multiOpen = multi_open;
	this.that = null;
	
	var close = function(item) {
		if (!isHidden(item)) {
			// Find the associated content and hide it
			var contentId = item.getElementsByTagName("A")[0].getAttribute("href").replace(/.*#/,"");
			
			document.getElementById(contentId).className += " "+hideClass; 
			
			// Desactivated stack panel item 
			item.className = item.className.replace(new RegExp(activeClass),"");
		}
	}; 
	
	var open = function(item) {
		if (isHidden(item)) {
			// Find the associated content and show it
			var contentId = item.getElementsByTagName("A")[0].getAttribute("href").replace(/.*#/,"");
			document.getElementById(contentId).className = document.getElementById(contentId).className.replace(new RegExp(hideClass),""); 

			// Activated stack panel item 
			item.className += " "+activeClass;
		}
	};
	
	this.show = function(item, visible) {
		if (visible) {
			open(item);
		}
		else {
			close(item);
		}
	}
	
	var isHidden = function(item) {
		if (!item.className.match(new RegExp(activeClass)))
			return true;
		else return false;
	};
	
	this.captureEvent = function(e) {
		// Standardize event
		e = e||window.event;
		var target = e.target||e.srcElement;
		
		// Check if item clicked is a item link
		if (target.className.match(new RegExp(linkClass))) {
		
			// Find the parent stack panel item 
			while (!target.nodeName.match(/^LI$/i)) 
				target = target.parentNode;
			
			// Root event to the callback
			traitementEvent(e, target);
			
			// Cancel default link action
			if (e.preventDefault) 
				e.preventDefault();
			else 
				e.returnValue = false;
			return false;
		}
	};
	
	var traitementEvent = function(e,target) {
		if(multiOpen) {
			that.show(target,isHidden(target));
		} else {
			var items = document.getElementById(containerId).getElementsByTagName("LI");
			for(var i=0; i<items.length; i++) {
				if(items[i].className.match(new RegExp(itemClass))) {
					if(items[i] != target) {
						close(items[i]);
					} else {
						that.show(target,isHidden(target)); 
					}
				}
			}
		}
	};
	
	this.init = function() {
		// Check that stackpanel exist
		if (document.getElementById(containerId)) {
			// Keep a reference on instance
			that = this;
		
			/* TODO : optim code pour G3R1 */
			if(!multi_open){
				var items = document.getElementById(containerId).getElementsByTagName("LI");
				for(var i=1; i<items.length; i++) {
				if(items[i].className.match(new RegExp(itemClass))) {
							close(items[i]);		
					}
				}
			}

		// optimisation possible: this.traitementEvent("",(document.getElementById(containerId).getElementsByTagName("LI"))[0]);
			
			// Register event
			var container = document.getElementById(containerId);
			if(container.addEventListener)
				container.addEventListener("click", this.captureEvent, false);
			else if(container.attachEvent)
				container.attachEvent("onclick",this.captureEvent);
			else container.onclick = this.captureEvent;
		} else return null;
	};
	return this.init();
}

/*
* Fonction d'initialisation
*/
var widget = new Array();
function init() {
	if (document.getElementById("stackpanel-multi"))
		widget.push(new StackPanel("stackpanel-multi", true));
	if (document.getElementById("stackpanel-single"))
		widget.push(new StackPanel("stackpanel-single", false));

}

/* initialisation du code JS non intrusif au chargement de la page */
/**
 * addOnLoad
 * Adds load to the page
 * tries to add it as soon as the dom has loaded if possible
 * else adds it to the window.onload stack
 * inspired by the discussion at http://dean.edwards.name/weblog/2006/06/again/
 * @param func function to be added
 * @return nothing
 */
function addOnLoad(/*function*/func) {
	ignited=false;
	/* for Mozilla/Opera9 */
	if (document.addEventListener  && !ignited) {
	  document.addEventListener("DOMContentLoaded", func, false);
	  //Debug.log("addEventListener triggered");
	  ignited=true;
	}
	
	/* For IE not so rusty */
	if(window.attachEvent && !ignited) {
		window.attachEvent('onload',func);
		ignited=true;
	}
	/* end */
	
	/* for Internet Explorer */
	/*@cc_on @*/
	/*@if (@_win32)
	  document.write("<script id=__ie_onload defer src=javascript:void(0)><\/script>");
	  var script = document.getElementById("__ie_onload");
	  script.onreadystatechange = function() {
	    if (this.readyState == "complete" && !ignited) {
	      func(); // call the onload handler
	      ignited=true;
	    }
	  };
	/*@end @*/
	
	/* for Safari */
	if (/WebKit/i.test(navigator.userAgent) && !ignited) { // sniff
	  var _timer = setInterval(function() {
	    if (/loaded|complete/.test(document.readyState) && !ignited) {
	      func(); // call the onload handler
	      ignited=true;
	    }
	  }, 10);
	}
	
	if(!ignited) {
		oldonload = window.onload;
		if (typeof window.onload != 'function') {
			window.onload = func;
		} else {
			window.onload = function(){
				if (oldonload) {
					oldonload();
				}
				func();
			}
		}
		ignited = true;
	}
}
addOnLoad(init);


