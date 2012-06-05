/*
 * Initialize events and elements in the GUI
 */
function setupGUI(){
	var canvas = document.createElement('canvas');  
	canvas.width = '50';
	canvas.height = canvas.width;
	var ctx = canvas.getContext("2d"); 
	ctx.fillStyle = "#BBB";
	ctx.fillRect(0,0,canvas.width,canvas.height);
	ctx.fillStyle = "#DDD";
	for(var i = 0; i < canvas.width; i += 4){
		ctx.beginPath();
		ctx.arc( i + 2, 2, 1, 0, 2*Math.PI, true);
		ctx.fill();
		ctx.beginPath();
		ctx.arc( 2, i + 2, 1, 0, 2*Math.PI, true);
		ctx.fill();
	}
	app.background = 'url('+canvas.toDataURL()+')';
	delete(ctx);
	delete(canvas);
	document.body.style.background = app.background;
	
	var bts = document.getElementsByClassName('x-button');
		if(device.emu){
			for(var i = 0; i < bts.length; i++){
			bts[i].onmousedown = function(){
				this.tmpClass = this.getAttribute('class');
				this.setAttribute('class', this.tmpClass + ' x-button-pressed');
			}
			bts[i].onmouseup = function(){
				if(this.tmpClass)
					this.setAttribute('class', this.tmpClass);
			}
			bts[i].onmouseout = bts[i].onmouseup;
			}

	}else{
		for(var i = 0; i < bts.length; i++){
			bts[i].ontouchstart = function(){
				this.tmpClass = this.getAttribute('class');
				this.setAttribute('class', this.tmpClass + ' x-button-pressed');
			}
			bts[i].ontouchend = function(){
				if(this.tmpClass)
					this.setAttribute('class', this.tmpClass);
			}
			bts[i].ontouchleave = bts[i].onmouseup;
		}
	}
}

function setCameraPopup(cam){
	if(!app.cameraPopup){
		app.cameraPopup = document.createElement('div');
		var label = document.createElement('div');
		label.style.display = 'inline';
		//label.style.lineHeight = '47px';
		//label.style.verticalAlign = 'center';
		app.cameraPopup.appendChild(label);
		var img = document.createElement('div');
		img.style.display = 'inline-block';
		img.style.width = '47px';
		img.style.height = '47px';
		img.style.marginLeft = '8px';
		img.style.backgroundImage = 'url(data:image/gif;base64,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)';
		app.cameraPopup.appendChild(img);
		app.cameraPopup.id = 'camerapopup';
		navigator.map.overlay.appendChild(app.cameraPopup);
	}
	app.cameraPopup.style.display = 'block';
	app.cameraPopup.style.left = (cam.marker.x - app.markerMargin)+'px';
	app.cameraPopup.style.top = (cam.marker.y + 10)+'px';
	app.cameraPopup.childNodes[0].innerHTML = cam.name;
	
	app.cameraPopup.childNodes[1].ontouchstart = function(){
		unsetCameraPopup();
		navigator.notification.confirm('Blitzer loeschen', function(choice){
				if(choice == 1) deleteCamera(cam);
		}, cam.name, 'Loeschen,Abbrechen');
	}

	navigator.map.onzoom = function() { setCameraPopup(cam); };

	app.cameraPopup.childNodes[1].onclick = function(){
		alert('Camera will be deleted');
		unsetCameraPopup();
		deleteCamera(cam);
	}
}

function unsetCameraPopup(){
	if(app.cameraPopup)
		app.cameraPopup.style.display = 'none';
}
