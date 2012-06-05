// Place your application-specific JavaScript functions and classes here
// This file is automatically included by javascript_include_tag :defaults

$(document).ready(function() {
  $('.modal').modal('hide');
  
  $("input#all").click(function() {
    $("input.all").attr("checked", $(this).attr("checked") == "checked")
  });
  
  $("input.all").click(function() {
    $("input#all").attr("checked", $("input.all:checked").length == $("input.all").length)
  });
});

