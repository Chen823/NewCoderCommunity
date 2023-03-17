
function like(btn, entityType, entityId) {
    $.post(
        CONTEXT_PATH + '/like',
        {"entityType": entityType, "entityId": entityId},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $(btn).children("b").text(data.likeCount);
                $(btn).children("i").text(data.likeStatus == 0 ? '赞' : '已赞');
            } else {
                alert(data.msg)
            }
        }
    )
}