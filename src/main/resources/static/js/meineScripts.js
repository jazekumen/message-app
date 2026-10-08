$(document).ready(function() {

    // Tooltips (only if the PowerTip plugin loaded)
    if ($.fn.powerTip) {
        $("input, textarea").powerTip();
    }

    // Zeichenzähler für das Nachrichteneingabefeld
    $("#content, #messageText").on("input", function() {
        var charCount = $(this).val().length;
        $("#charCount").text("Zeichenanzahl: " + charCount);
    });
});
