
function like(btn, entityType, entityId, entityUserId, postId) {
    $.post(
        CONTEXT_PATH + '/like',
        {"entityType": entityType, "entityId": entityId, "entityUserId":entityUserId, "postId":postId},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $(btn).children("i").text(data.likeStatus == 0 ? '赞' : '已赞');
                $(btn).children("b").text(data.likeCount);
            } else {
                alert(data.msg)
            }
        }
    )
}