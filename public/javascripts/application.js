$(document).ready(function() {
	if ($('.modal').length > 0)
  	$('.modal').modal('hide');
  
  $("input#all").click(function() {
    $("input.all").attr("checked", $(this).attr("checked") == "checked")
  });
  
  $("input.all").click(function() {
    $("input#all").attr("checked", $("input.all:checked").length == $("input.all").length)
  });

   
  document.body.innerHTML = document.body.innerHTML.replace(/TextBuster/g,'TextBuster&reg;');
});

