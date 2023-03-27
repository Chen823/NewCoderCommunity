$(function(){
    $(".top-btn").click(setTop);
    $(".wonderful-btn").click(setWonderful);
    $(".delete-btn").click(deletePost);
});

function setTop() {
    var btn = this;
    if($(btn).hasClass("btn-success")) {
        // 关注TA
        //$(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
        $.post(
            CONTEXT_PATH + '/discuss/top',
            {"postId": $(btn).prev().val(), "type":0},
            function (data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        );
    } else {
        // 取消关注
        //$(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
        $.post(
            CONTEXT_PATH + '/discuss/top',
            {"postId": $(btn).prev().val(), "type":1},
            function (data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg)
                }
            }
        );
    }
}

function setWonderful() {
    var btn = this;
    if($(btn).hasClass("btn-success")) {
        $.post(
            CONTEXT_PATH + '/discuss/wonderful',
            {"postId": $(btn).prev().prev().val(), "status":0},
            function (data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        );
    } else {
        $.post(
            CONTEXT_PATH + '/discuss/wonderful',
            {"postId": $(btn).prev().prev().val(), "status":1},
            function (data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg)
                }
            }
        );
    }
}

function deletePost() {
    var btn = this;
        $.post(
            CONTEXT_PATH + '/discuss/delete',
            {"postId": $(btn).prev().prev().prev().val()},
            function (data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    location.href = CONTEXT_PATH + "/index"
                } else {
                    alert(data.msg);
                }
            }
        );
}