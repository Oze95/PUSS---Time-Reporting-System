function myalert(){
	window.alert("test");
}

var toggled = false;

// can probably be done better but it works :D
function toggle(obj){
	var el = document.getElementById(obj);

	if ( el.style.display = 'none' && toggled==false ) {
		el.style.display = ''; //shows
		toggled=true;
		return;
	}

	if(el.style.display != 'none' && toggled==true){
		el.style.display = 'none'; //hides
		toggled=false;
		return;

	}
}

function isNumber(evt) {
    evt = (evt) ? evt : window.event;
    var charCode = (evt.which) ? evt.which : evt.keyCode;
    if (charCode > 31 && (charCode < 48 || charCode > 57)) {
        return false;
    }
    return true;
}

function findWeek() {

 var radios = document.getElementsByName('weekselectors');
 var row;

 for (row = 0; row < radios.length; row++) {
  if (radios[row].checked == true) {
	  document.getElementById('SelectedWeek').value = radios[row].value;
  }
 }
}
