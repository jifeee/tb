if(!window.device){
	window.device = {
		uuid: '67-UD-fdf-sd-sd-sd-sd-sd-sd'
	}
};

var initTime = new Date().getTime();

var macs = {};
var synclogs = [];
var syncinterval = 60000;

function error(msg){
	log(msg, 'error');
}

function log(msg, cls){
	var time = Math.round((new Date().getTime() - initTime) / 100) / 10;
	time = '' + time + ((time % 1 == 0) ? '.0' : '');
	
	var span = document.createElement('p');
	span.innerHTML = '<div class="time">' + time + '</div> ' + msg;
	if(cls) span.setAttribute('class', cls);
	
	var log = document.getElementById('log');
	if(log.firstChild && log.firstChild.nextSibling){
		log.insertBefore(span, log.firstChild.nextSibling);
	}else{
		log.appendChild(span);
	}
	
	if(window.logfile){
		window.logfile.write(time + ' | ' + msg + "\n");
	}
}

function registerAcl(){
	PhoneGap.exec(function(json){
		var time = new Date().getTime();
		
		var res = JSON.parse(json);
		
		res.uuid = device.uuid;
		res.time = time;
		
		var action = res.action;
		var address = res.address;
		
		if(!action) action = 'UN';
		if(!address) address = 'n/a';
		
		if(action == "ACCN"){
			macs[res.address] = time;
		}
		
		if(action == "ACDC"){
			if(macs[res.address]){
				res.period = Math.round((time - macs[res.address]) / 1000);
			}else{
				res.period = -1
			}
		}
		
		if(action == "INIT"){
			res.device = window.device;
		}
		
		if(action == 'LOCK'){
			document.getElementById('panel').style.background = 'red';
		}
		if(action == 'UNLO'){
			document.getElementById('panel').style.background = null;
		}
		
		synclogs.push(res);
		
		var messages = {
			'INIT' : 'ACL listener registered',
			
			'RFSK' : 'Skipping to create Rfcomm ' + address,
			'RFTR' : 'Trying to create Rfcomm connection to ' + address,
			'RFCL' : 'Close Rfcomm connection',
			'RFCN' : 'Rfcomm connection successful',
			'RFAB' : 'Rfcomm connection aborted',
			'RFNO' : 'Rfcomm connection was already closed.',
			'RFIP' : 'Rfcomm connection dropped due to ongoing connection.',
			
			'ACCN' : 'ACL connected from ' + address,
			'ACDR' : 'ACL disconnect requested from ' + address,
			'ACDC' : 'ACL disconnected from ' + address + " after " + res.period + "s",
			
			'LSEM' : 'No stored addresses, skipping discovery',
			'LSTR' : 'Next Textbuster MAC ' + address,
			
			'LOCK' : 'Locking screen',
			'UNLO' : 'Unlocking screen'
		};
		
		if(messages[action]){
			log(messages[action]);
		}else{
			log(action)
		}	
	}, function(){
		error('Registering ACL listener failed');
	}, 'BlueScan', 'registerACL', []);
}

function initialize(){
	window.logfile = null;
	
	var d = new Date();
	
	var logname = "BlueScan-" + d.getMonth() + "-" + d.getDate() + "-" + d.getFullYear() + "_" + d.getHours() + "-" + d.getMinutes() + "-" + d.getSeconds() + ".log";
	
	var fail = function(msg){
		alert('Log file disabled due to internal error [' + msg.code + ']');
		console.log('Log file disabled due to internal error [' + msg.code + ']');
	};
	
	window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, function(fs){
		fs.root.getFile(logname, {create: true, exclusive: false}, function(fe){
			fe.createWriter(function(fw){
				window.logfile = fw;
			}, fail);
		}, fail);
	}, fail);
	
	registerAcl();
}

function syncNow(){
	
	if(synclogs.length == 0){
		 console.log('Nothing to sync');
		 setTimeout(syncNow, syncinterval);
		 return;
	}
	
	console.log("Syncing " + synclogs.length + " items");
	
	var xhr = new XMLHttpRequest();
	xhr.open('POST', 'http://bluescanlog:test1234@sklick.de:5999/bluescanlog/_bulk_docs', true);
	xhr.setRequestHeader('Content-Type', 'application/json');
	
	xhr.onerror = function(e){
		alert("Sync Error: " + e);
	}
	xhr.onreadystatechange = function(){
		if (xhr.readyState === 4) {  
			if(xhr.status == 201)
				synclogs = [];
			setTimeout(syncNow, syncinterval);
		}
	}
	xhr.send(JSON.stringify({"docs":synclogs}));
};
setTimeout(syncNow, syncinterval);

function init() {
    document.addEventListener("deviceready", initialize, true);
    
    document.body.onresize = function(){
		document.body.style.height = window.innerHeight + 'px';
	}; document.body.onresize();
	
	log('&nbsp;', 'head');
	
	var updateTime = function(){
		var time = Math.round((new Date().getTime() - initTime) / 100) / 10;
		time = '' + time + ((time % 1 == 0) ? '.0' : '');
		
		document.getElementById('log').firstChild.firstChild.innerHTML = time;
		setTimeout(updateTime, 100);
	}; updateTime();
	
	document.getElementById('swi-ctrl').handler = function(){
		if(this.pressed == true){
			document.getElementById('controls').style.height = parseInt(getComputedStyle(document.getElementById('log')).height) / 2 + 'px';
		}else{
			document.getElementById('controls').style.height = 0;
		}
	}
	
	document.getElementById('btn-discover').handler = function(){
		PhoneGap.exec(function(result){},
		function(){
			error('startDiscovery() failed');
		}, 'BlueScan', 'startDiscovery', []);
	}
	
	document.getElementById('btn-rfcomm').handler = function(){
		var mac = document.getElementById('inp-mac').value.toUpperCase();
		
		if(mac.match(/^[0-9A-F]{2}:[0-9A-F]{2}:[0-9A-F]{2}:[0-9A-F]{2}:[0-9A-F]{2}:[0-9A-F]{2}$/)){
			
			PhoneGap.exec(function(result){},
			function(){
				error('createRfcomm...() failed');
			}, 'BlueScan', 'createRfcomm', [mac]);
		}else{
			alert('Malformed MAC. Format: "00:11:22:33:44:55"');
		}
	}
	
	document.getElementById('btn-saveset').handler = function(init){
		var mac = document.getElementById('inp-mac').value.toUpperCase();
		var int1 = parseInt(document.getElementById('inp-int1').value);
		var int2 = parseInt(document.getElementById('inp-int2').value);
		
		if(mac.match(/^[0-9A-F]{2}:[0-9A-F]{2}:[0-9A-F]{2}:[0-9A-F]{2}:[0-9A-F]{2}:[0-9A-F]{2}$/)){
			localStorage.setItem('mac', mac);
			localStorage.setItem('int1', int1);
			localStorage.setItem('int2', int2);
			
			PhoneGap.exec(null, null, 'BlueScan', 'setVariables', [mac, int1, int2]);
			
			if(!init)alert('Settings saved.');
		}else{
			alert('Malformed settings (MAC: "00:11:22:33:44:55" Intervals: "120")');
		}
	}
	
	if(localStorage.getItem('mac')) document.getElementById('inp-mac').value = localStorage.getItem('mac');
	if(localStorage.getItem('int1')) document.getElementById('inp-int1').value = localStorage.getItem('int1');
	if(localStorage.getItem('int2')) document.getElementById('inp-int2').value = localStorage.getItem('int2');
	
	document.getElementById('btn-saveset').handler(true);
	
	var buttons = document.getElementsByClassName('button');
	for(var i = 0; i <  buttons.length; i++){
		buttons[i].ontouchstart = buttons[i].onmousedown = function(){
			this.setAttribute('class', this.getAttribute('class') + ' pressed');
		}
		buttons[i].ontouchend = buttons[i].onmouseup = function(){
			this.setAttribute('class', this.getAttribute('class').replace(' pressed', ''));
			
			if(typeof(this.handler) == 'function') this.handler.call(this);
		}
		
		if("ontouchstart" in document.documentElement){
			buttons[i].onmouseup = buttons[i].onmousedown = null;
		}
	}
	
	var switchs = document.getElementsByClassName('switch');
	for(var i = 0; i <  switchs.length; i++){
		switchs[i].ontouchstart = switchs[i].onmousedown = function(){
			if(this.pressed != true){
				this.setAttribute('class', this.getAttribute('class') + ' pressed');
			}
		}
		switchs[i].ontouchend = switchs[i].onmouseup = function(){
			if(!this.pressed || this.pressed == false){
				this.pressed = true;
			}else{
				this.pressed = false;
				this.setAttribute('class', this.getAttribute('class').replace(' pressed', ''));
			}
			
			if(typeof(this.handler) == 'function') this.handler.call(this, this.pressed);
		}
		
		if("ontouchstart" in document.documentElement){
			switchs[i].onmouseup = switchs[i].onmousedown = null;
		}
	}
	
	alert('Android BlueScan is a Mobilezapp product.<br><br>Do not propagate without permission.<br><br>Please contact:<br>sascha.klick@mobilezapp.com<br>for further information.', 3000);
}

window.alert = function(msg, duration){
	var div = document.createElement('div');
	div.setAttribute('class', 'alert');
	div.innerHTML = msg;

	document.body.appendChild(div);

	div.style.webkitTransform = 'translate3d(' + Math.round((window.innerWidth / 2) - (parseInt(getComputedStyle(div).width) / 2)) + 'px,' + Math.round((window.innerHeight / 2) - (parseInt(getComputedStyle(div).height) / 2)) + 'px,0)';
	
	setTimeout(function(){
		div.style.webkitTransition =  '-webkit-transform .5s ease-in, opacity .5s ease-in';
		div.style.opacity = 0;
		//div.style.webkitTransform = 'translate3d(' + window.innerWidth + 'px,' + ((window.innerHeight / 2) - (parseInt(getComputedStyle(div).height) / 2)) + 'px,0)';
		
		setTimeout(function(){
			div.parentNode.removeChild(div);
		}, 500);
	}, (duration) ? duration : 1000);
	
}
