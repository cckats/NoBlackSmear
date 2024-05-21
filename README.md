# NoBlackSmear

"Fix" for the Black Smear effect caused by green pixel charge delay on many Amoled panels at low brightness levels 
The problem arrises from the small difference in "wake-up" delay from black that the different subpixels have and causing a deep purple hue at the edges of gray objects
<p align="center">
  <img src="https://github.com/cckats/NoBlackSmear/assets/39501174/1c667483-ccff-4680-8070-1ac123ee6803">
</p>

This also fixes as a side effect the Black Crush effect caused by wrong screen calibration at low brightness levels which removes detail in the shadows

This is achieved by using a semi transparent software layer that fills the screen causing the black levels to rise from 0 to bearly perseptable levels and thus forcing the pixels to stay "on" removing this small "wake-up" delay

For best results the strength (in white luminosity) of this semi transparent software layer is dynamicly adjusted and turned off based on the screen brightness and can be further fine-tuned for different screens

<p align="center">
  <img src="https://github.com/cckats/NoBlackSmear/assets/39501174/111c9749-2805-44df-8eaa-cd12cdd719f4">
</p>

If you suffering from this issue just Download the app enable the overlay and adjust to your liking :)
