function get_style(mobile) {
	if(mobile) {
		style = '*.controlbutton{width:64px !important;height:64px;float:right;}';
		style +='#controller{	z-index:10;	width:590px; border-top:solid 1px #b8b8b8; position:relative; padding:5px; background-color:#f0f0f0; opacity:1.0; top:524px; height:65px;}';
		style +='#dOpacityController {left:90px;right:400px;}';
		style +='.ui-slider .ui-slider-handle { width: 64px; height: 64px; } .ui-slider-horizontal .ui-slider-handle { top: -12px; margin-left: -32; text-align:center; text-decoration:none; } .ui-slider-horizontal { height:36px; }';
		
	}
	else {
		style = '*.controlbutton{width:34px !important;height:34px;float:right;}';
		style +='#controller{	z-index:10;	width:590px; border-top:solid 1px #b8b8b8; position:relative; padding:5px; background-color:#f0f0f0; opacity:0.9; top:554px; height:35px;}';
		style +='#dOpacityController {left:50px;right:220px;}';
		
	}
	//style +='#magazine{ width:600px; height:600px; }';
	//style +='#magazine .turn-page{ background-color:'+turnjs_bgcolor+'; background-size:100% 100%; }';
	return style;
}

function turnjs_animation_event() {
	SDT_animation_event("turnjs_animation_event_before","turnjs_animation_event_after");
}
function turnjs_animation_event_before() {
	//log.append("ANIM start<br>");
	if(!magazineFolding) {
		SDO_hide_overlay();
		magazine.hide();
	}
}
function turnjs_animation_event_after() {
	if(!magazineFolding) {
		//log.append("ANIM end<br>");
		//console.log(override_unlock_animation);
		if(!override_unlock_animation)
			SDO_show_overlay();
		//Get image infomation
		//alert('123123');
		SDT_get_info();
		if(!override_unlock_animation) {
			turnjs_align();
			if(magazine_mode == 2) { //Double page mode
				if(turnjs_centering_right_page() == current_page%2) { //Current page is out of center
					//log.append("Out of center<br>");
					turnjs_out_of_center();
				}
				else {
					//log.append("In center<br>");
				}
			}	
		}
	}
}
function turnjs_out_of_center() {
	if(current_page%2 == 1 && current_page < total_page) { //Currently on the left page and not the last page
		current_page++;
		image_left += image_width;
		turnjs_openDzi();
		//setTimeout(turnjs_openDzi,1000);
		//setTimeout(turnjs_openDzi,1000);
		return true;
	}
	else if(current_page%2 == 0 && current_page > 0) {// Not the first page
		//log.append('pg'+current_page+"<br>");
		current_page--;
		//log.append('pg'+current_page+"<br>");
		//log.append(image_left+"<br>");
		image_left -= image_width;
		//log.append(image_left+"<br>");
		turnjs_openDzi();
		//setTimeout(turnjs_openDzi,1000);
		return true;
	}
	return false;
}

function turnjs_align() {
	magazine_pos.x = image_left-(magazine_mode-1)*image_width*((current_page+1)%2);
	magazine_pos.y = image_top;
	magazine.flip("set_position",magazine_pos.x,magazine_pos.y);
	magazine.flip('zoom_by',image_scale,image_width,image_height);
	magazine.show();
}


var magazine,dMagazine;
var magazinedrag_original = {x:0,y:0};
var magazine_moveby = {x:0,y:0};
var magazine_pos = {x:0,y:0};
var magazine_mode = 1; //1 Single 2 Double
var magazine_manual_turn = false;
var turnjs_bgcolor = "#000";
var page_pos = {x:0,y:0};
var current_page = 0;
var total_page = 0;
//var base_resources = ['001-1.xml','002-1.xml','003-1.xml','001-1.xml','002-1.xml'];
//var overlay_resources = ['001-2.xml','002-2.xml','003-2.xml','001-2.xml','002-2.xml'];
var magazineFolding = false;

function turnjs_setbgcolor(color) {
	turnjs_bgcolor = color;
}


/*function turnjs_openDzi() {
	log.append('turnjs opendzi pg'+current_page+'<br>');
	image_isOpened = false;
	//new_pos = base.viewport.getCenter();
	//log.append(new_pos.x+" "+new_pos.y+"<br>");
	$('.turn-page').css('background-color',turnjs_bgcolor);
	dBase.hide();
	dOverlay.hide();
	base.openDzi(base_resources[current_page]);	
	overlay.openDzi(overlay_resources[current_page]);
	turnjs_align_seadragon();
}*/
function turnjs_openDzi() {
	$('#loader').html('Loading Page '+(current_page+1)+' From '+total_page);
	$('#loader').fadeIn();
	//log.append(image_left+"<br>");
	//log.append('turnjs opendzi pg'+current_page+'<br>');
	image_isOpened = false;
	//new_pos = base.viewport.getCenter();
	//log.append(new_pos.x+" "+new_pos.y+"<br>");
	dMagazine.css("z-index",100);
	$('.turn-page').css('background-color',turnjs_bgcolor);
	turnjs_wait_until_ready_to_open();
	//base.openDzi(base_resources[current_page]);	
	//overlay.openDzi(overlay_resources[current_page]);
	//log.append(image_left+"<br>");
	//turnjs_align_seadragon();
	
}


function turnjs_wait_until_ready_to_open() {
	//console.log('C');
	if(base.profiler.isMidUpdate() || overlay.profiler.isMidUpdate()) {
		//log.append('turnjs waiting<br>');	
		setTimeout(turnjs_wait_until_ready_to_open,1000);
	}
	else {
		//base.close();
		//overlay.close();
		base.openDzi(base_resources[current_page]);	
		overlay.openDzi(overlay_resources[current_page]);
		//log.append('done_opening<br>');	
		turnjs_align_seadragon();
	}
}

function turnjs_align_seadragon() {
	if(!base.isOpen()) {
		//log.append('turnjs base opening<br>');	
		setTimeout(turnjs_align_seadragon,1000);
	}
	else if(!overlay.isOpen()) {
		//log.append('turnjs overlay opening<br>');	
		setTimeout(turnjs_align_seadragon,1000);
	}
	else {
		//log.append('ALIGNSS seadragon<br>');
		//log.append('ALIGNSS seadragon<br>');
		image_isOpened = true;
		goal_scale = image_scale; //Goal scale and position
		goal_left = image_left;
		goal_top = image_top;
		SDT_get_info(); //Current scale and position
		//Calculate static point (does not move when zoom)
		//In viewport coordinate
		diff_scale = goal_scale-image_scale;
		diff_left = image_left-goal_left;
		diff_top = image_top-goal_top;
		
		if(diff_scale != 0) {
			static_left = goal_left+(goal_scale/diff_scale)*diff_left;
			static_top = goal_top+(goal_scale/diff_scale)*diff_top;
			//Convert to image point Seadragon system
			pt_l = (static_left-(view_width-image_width)/2)/image_width;
			pt_t = (static_top-(view_height-image_height)/2)/image_width;
			base.viewport.zoomTo(goal_scale,new Seadragon.Point(pt_l, pt_t),true);
			//log.append('static L:'+static_left+' T:'+static_top+'<br>');
		}
		else {
			//No need to magnify, Only pan the image
			pt_l = diff_left/image_width;
			pt_t = diff_top/image_width;
			base.viewport.panBy(new Seadragon.Point(pt_l, pt_t),true);
		}
			
		//log.append('Goal S:'+goal_scale+' L:'+goal_left+' T:'+goal_top+'<br>');
		//log.append('Image S:'+image_scale+' L:'+image_left+' T:'+image_top+'<br>');
		//log.append('Diff S:'+diff_scale+' L:'+diff_left+' T:'+diff_top+'<br>');
		
		//log.append('point L:'+pt_l+' T:'+pt_t+'<br>');
		
		//For slow device, it update the image information too slow.
		//We need to allow it update whenever animation is complete
		override_unlock_animation = true;
		dMagazine.css("z-index",-100);
		$('#loader').fadeOut();
		//log.append('DONE<br>');
		/*
		image_scale = current_image_scale; // Preserve current zoom level
		pos = magazine.flip("get_position");
		log.append("posx"+pos.x+" poxy"+pos.y+"<br>");
		log.append("width"+image_width+"scale"+image_scale+"<br>");
		pos_left = image_width/2-pos.x+(view_width-image_width)/2;
		pos_top = image_height/2-pos.y+(view_height-image_height)/2;
		pos_pt_left = pos_left/image_width;
		pos_pt_top = pos_top/image_width;
		log.append("L"+pos_left+" T"+pos_top+"<br>");
		log.append('LL'+pos_pt_left+" TT"+pos_pt_top+"<br>");
		base.viewport.panTo(new Seadragon.Point(pos_pt_left, pos_pt_top),true);
		log.append('DONE<br>');
		*/
	}
}



function turnjs_centering_right_page() {
	if(magazine_pos.x+image_width < view_width/2) //On the right side
		return 1;
	else
		return 0;
}

function turnjs_next() {
	magazine_manual_turn = true;
	if(magazine_mode == 1) { //single page mode
		if(current_page < total_age-1) {
			current_page ++;
			magazine.turn('next');
		}
	}
	else { //double page mode
		if(turnjs_centering_right_page() == 1) { //On the right
			if(current_page < total_page-2) { // Have next right page
				current_page += 2;
				magazine.turn('next');
			}
			else if(current_page < total_page-1) { // Only have next left page
				current_page++;
				image_left -= image_width;u
				magazine.turn('next');
				//alert("FAILED IMPLEMENT turnjs_next()");
			}
		}
		else { //On left page
			if(current_page < total_page-2) { // Allow turn only have next left page
				current_page += 2;
				magazine.turn('next');
			}
		}
	}
}

function turnjs_prev() {
	magazine_manual_turn = true;
	if(magazine_mode == 1) { //single page mode
		if(current_page > 0) {
			current_page --;
			magazine.turn('previous');
		}
	}
	else { //double page mode
		if(turnjs_centering_right_page() == 0) { //On the left
			if(current_page > 1) { // Have next left page
				current_page -= 2;
				magazine.turn('previous');
			}
			else if(current_page > 0) { // Only have next right page
				current_page --;
				image_left += image_width;
				magazine.turn('previous');
				//alert("FAILED IMPLEMENT turnjs_next()");
			}
		}
		else { //On left page
			if(current_page > 1) { // Allow turn only have next right page
				current_page -= 2;
				magazine.turn('previous');
			}
		}
	}
}

var loadingjscssfile = 0;

// http://www.javascriptkit.com/javatutors/loadjavascriptcss.shtml
function loadjscssfile(filename, filetype){
 if (filetype=="js"){ //if filename is a external JavaScript file
  var fileref=document.createElement('script');
  fileref.setAttribute("type","text/javascript");
  fileref.setAttribute("src", filename+'.'+filetype);
 }
 else if (filetype=="css"){ //if filename is an external CSS file
  var fileref=document.createElement("link");
  fileref.setAttribute("rel", "stylesheet");
  fileref.setAttribute("type", "text/css");
  fileref.setAttribute("href", filename+'.'+filetype);
 }
 if (typeof fileref!="undefined")
  fileref.setAttribute("onload","turnjs_onloaded()");
  loadingjscssfile++;
  document.getElementsByTagName("head")[0].appendChild(fileref);
}

function turnjs_onloaded() {
	loadingjscssfile--;
}

function turnjs_onload(page) {
	//alert('turnjs_onload');
	
	//Set totalpage
	total_page = page;
	
	//loadjscssfile("js/seadragon-dev", "js");
	loadjscssfile("js/seadragon.touch", "js");
	loadjscssfile("js/seadragon.overlay", "js");
	loadjscssfile("js/turn", "js");
	loadjscssfile("js/hammer", "js");
	loadjscssfile("js/jquery.hammer", "js");
	loadjscssfile("js/turn", "css");

	setTimeout("turnjs_onload_done()",1000);
}

function turnjs_onload_done() {
	if(loadingjscssfile != 0) {
		setTimeout("turnjs_onload_done",1000);
	}
	else {
		dBase = $('#dBase');
		dOverlay = $('#dOverlay');
		log = $('#log');
	
		base = new Seadragon.Viewer("dBase");
		overlay = new Seadragon.Viewer("dOverlay");
		//base.openDzi("001-1.xml");
		//overlay.openDzi("001-2.xml");
	
		Seadragon.Config.animationTime = const_animationTime;
		Seadragon.Config.springStiffness = 10;
		Seadragon.Config.visibilityRatio = 0;
		Seadragon.Config.imagePath = "img/";
	
		SDT_init(dBase,base);
		SDT_enable_touch(dBase);
		SDO_init(dBase,base,dOverlay,overlay);
		SDO_openDzi(base_resources[0],overlay_resources[0],function() {	
			//SDO_animation_event(); //Enable touch feature with overlay
			turnjs_animation_event();
			
			turnjs_init($('#magazine'),$('#d_magazine'));
			turnjs_setbgcolor('#cff');
			
			SDO_create_opacity_controller($('#opacityController'),$(".overlayed"));
			SDO_custom_style('get_style');
			SDO_implement_controller($('#controller'));
		});
	}
}

function turnjs_init(mz,dmz) {
	magazine = mz;
	dMagazine = dmz;
	magazine.turn({
		display: 'double',
		acceleration: true,
		gradients: true,
		/*gradients: !$.isTouch,*/
		elevation:50,
		shadows: true,
		when: {
			turned: function(e, page) {
				$('.turn-page').css('background-color','');
				dBase.fadeIn();
				dOverlay.fadeIn();
				/*console.log('Current view: ', $(this).turn('view'));*/
			}
		}
	});
	magazine_mode = 2;
	magazine_pos.x = (view_width-image_width)/2-(magazine_mode-1)*image_width*((current_page+1)%2);
	magazine_pos.y = (view_height-image_height)/2;
	//magazine.flip("set_position",magazine_pos.x,magazine_pos.y);
	magazine.flip("dimension",image_width,image_height);
	magazine.flip("viewport",view_width,view_height);
	magazine.flip("update_size",magazine);
	turnjs_align();
	
	
	magazine.bind("turning", function(event, page, view) {
		//log.append("TURNING");
		dBase.hide();
		dOverlay.hide();
	});
	magazine.bind("turned", function(event, page, view) {
		if(magazine_manual_turn) {
			magazine_manual_turn = false;
			turnjs_openDzi();
		}
		else {
			//log.append("TURNED");
			page--;
			//log.append(page);
			if(magazine_mode == 2) { // Double page
				if(page > current_page) // Forward
					if(page < total_page-1) // Not the last page
						current_page = page+1;
					else {
						current_page = page;
						image_left -= image_width;
					}
				else // Backward
					if(page > 0) // Not the first page
						current_page = page-1;
					else {
						current_page = page;
						image_left += image_width;
					}	
			}
			else
				current_page = page;
			turnjs_openDzi();
		}
		//current_page = page-1; // Check out of center
		/*if(!turnjs_out_of_center())
			 // Manually open page*/
	});
	
	if(!touch) {
		magazine.bind("start", function(event, pageObject, corner) {
			//console.log('START');
			//log.append("start");
			magazineFolding = true;
			seadragonController.animate({top:'+=45px',height:'0px',opacity:0} ,function() {
				//console.log('START-animate');
				dMagazine.css("z-index",1000);
				//dBase.hide();
				//dOverlay.hide();
				//console.log('START-animated');
			});
			//console.log('START-done');
		});
		
		magazine.bind("endzz", function(event, pageObject, turned) { //endzz
			//log.append("end");
			console.log('ENDZZ');
			magazineFolding = false;
			seadragonController.animate({top:'-=45px',height:'35px',opacity:1});
			dMagazine.css("z-index",-100);
			//Delay for seadragon
			//setTimeout(turnjs_align_seadragon,100);
			//dBase.show();
			//dOverlay.show();
		});
	}
	
	$(window).resize(function(){
		if(fullscreenMode) {
			setTimeout(do_zoomFit,200);
			view_width = window.innerWidth;
			view_height = window.innerHeight;
			/*if(window.innerHeight > window.innerWidth){
				magazine.flip("displaymode",1); //Portrait
			}
			else {
				magazine.flip("displaymode",2); //Landscape
			}*/
		}
		//resizeScreen();
	});
	
	
	
	//$('#magazine').flip("displaymode",0); 
	
	//Enable touch feature
	/*if(touch) {
		magazine.hammer({prevent_default: true, swipe:false, drag_vertical:false, transform:false ,tap_double:false, hold:false, drag_min_distance: 0});
		magazine.on('dragstart',do_magazinedragstart);
		magazine.on('drag',do_magazinedrag);
		//controller.on('dragend',do_opacitydragend);
		//magazine.on('tap',do_opacitydrag);
		magazine.on('release',switch_seadraon_turnjs);
	}*/
}

function switch_seadraon_turnjs() {
	dMagazine.hide();
	dBase.show();
	dOverlay.show();
}

function do_magazinedragstart(event) {
	magazinedrag_original= get_touchpoint(event);
	magazine_pos = magazine.flip("get_position");
}
function do_magazinedrag(event) {
	cur_pos = get_touchpoint(event);
	new_x = magazine_pos.x+cur_pos.x-magazinedrag_original.x;
	new_y = magazine_pos.y+cur_pos.y-magazinedrag_original.y;
	magazine.flip("set_position",new_x,new_y);
}
