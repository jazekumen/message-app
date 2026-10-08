// Polls the server every second and adds new messages to the top of the list.
// lastClientMessage is set by the page (timestamp of the newest message shown).
$(document).ready(function () {

    function pad(n) {
        return (n < 10 ? "0" : "") + n;
    }

    // Same format as the server-rendered list: dd.MM.yyyy HH:mm
    function formatDate(value) {
        var d = new Date(value);
        return pad(d.getDate()) + "." + pad(d.getMonth() + 1) + "." + d.getFullYear() +
            " " + pad(d.getHours()) + ":" + pad(d.getMinutes());
    }

    // Build elements with .text() so message content is never interpreted as HTML
    // (prevents users from injecting scripts into other people's pages).
    function createMessageItem(message) {
        var header = $("<header>")
            .append($("<span class='user'>").text(message.user.fullname || message.user.username))
            .append(" ")
            .append($("<span class='username'>").text("@" + message.user.username))
            .append($("<time>").attr("datetime", new Date(message.date).toISOString())
                .text(formatDate(message.date)));
        var item = $("<li class='ajaxNew post'>").hide()
            .append(header)
            .append($("<p>").text(message.text));
        if (message.location) {
            item.append($("<p class='location'>").text("Ort: " + message.location));
        }
        return item;
    }

    function loadMessages() {
        $.ajax({
            url: "/ajax/messages.json",
            data: { lastClientMessage: lastClientMessage },
            cache: false,
            success: function (messages) {
                if (messages && messages.length) {
                    lastClientMessage = new Date(messages[0].date).getTime();
                    var items = $.map(messages, createMessageItem);
                    // the newest message gets the rounded top corners
                    $("#messageList > li.first").removeClass("first");
                    items[0].addClass("first");
                    $("#messageList").prepend(items);
                    $(".ajaxNew").slideDown(600, "swing", function () {
                        $(this).removeClass("ajaxNew");
                    });
                }
            },
            complete: function () {
                setTimeout(loadMessages, 1000);
            }
        });
    }

    setTimeout(loadMessages, 1000);
});
