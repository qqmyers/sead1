function startTagCloud() {
        jQuery('#tagCloud').append('<div id="CanvasContainer"><canvas width="1000" height="400" id="tagCanvas"></canvas></div>');

          if(!jQuery('#tagCanvas').tagcanvas({
            textColour: '#0b5ca3',
            outlineColour: '#f3882a',
            reverse: true,
            depth: 0.8,
            maxSpeed: 0.05,
            textHeight: 25,
            weight: true,
            weightFrom: 'data-weight',
            weightSizeMax: 24,
            weightSizeMin: 6,
			weightSize: 10
          },'weightedtaglist')) {
   		   console.log('Error: could not create canvas');
            jQuery('#CanvasContainer').hide();
          }
  
	};