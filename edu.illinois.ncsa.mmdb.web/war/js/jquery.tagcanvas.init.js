function startTagCloud() {
        jQuery('#tagCloud').append('<div id="CanvasContainer"><canvas width="300" height="300" id="tagCanvas"></canvas></div>');

          if(!jQuery('#tagCanvas').tagcanvas({
            textColour: '#0b5ca3',
            outlineColour: '#f3882a',
            reverse: true,
            depth: 0.8,
            maxSpeed: 0.05,
            textHeight: 25,
            weight: true,
            weightFrom: 'data-weight'
          },'weightedtaglist')) {
   		   console.log('Error: could not create canvas');
            jQuery('#CanvasContainer').hide();
          }
  
	};
     
	 