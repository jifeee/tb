/*
 * Add a file to the include queue
 */
function include(file, runlevel) {
	var inc = new Object();
	inc.file = file;
	inc.status = 0;
	inc.runlevel = runlevel;
	includes.push(inc);
}

/*
 *	Start the next runlevel, load all needed includes first and execute
 *	a followup function once finished
 */
function runlevel(level, next) {
	var loading = 0;
	for(var i = 0; i < includes.length; i++){
		if(includes[i].runlevel == level){
			if (includes[i].status == 0){
				includes[i].status = 1;
	
				var s = document.createElement('script');
				s.setAttribute('type','text/javascript');
				s.setAttribute('src', includes[i].file);
	
				s.i = i;
				s.onload = function() {
					console.log(' Loaded include "'+includes[this.i].file+'" (Runlevel: '+includes[this.i].runlevel+')');
					includes[this.i].status = 2;
					this.parentNode.removeChild(this);
					if(next) runlevel(level, next);
				};
				document.getElementsByTagName('head')[0].appendChild(s);
				
				loading++;
			}else if (includes[i].status == 1)
				loading++;
		}
	}
	
	if(loading == 0 && next) next.call();
}
