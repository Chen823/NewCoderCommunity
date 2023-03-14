$(function () {
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
	$("#deleteMessageBtn").click(delete_letter);
});

function send_letter() {
	$("#sendModal").modal("hide");

	var toName = $("#recipient-name").val();
	var content = $("#message-text").val();
	$.post(
		CONTEXT_PATH + '/letter/send',
		{"toName": toName, "content": content},
		function (data) {
			data = $.parseJSON(data);
			if (data.code == 0) {
				$("#hintBody").text("发送成功");
			} else {
				$("#hintBody").text(data.msg);
			}

			$("#hintModal").modal("show");
			setTimeout(function () {
				$("#hintModal").modal("hide");
				location.reload();
			}, 2000);
		}
	)
}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}

function delete_letter() {

	var deleteMessageId = $("#deleteMessageId").val();
	$.post(
		CONTEXT_PATH + '/letter/delete',
		{"deleteMessageId": deleteMessageId},
		function (data) {
			data = $.parseJSON(data);
			if (data.code == 0) {
				$("#hintBody").text("删除完毕！");
			} else {
				$("#hintBody").text(data.msg);
			}

			$("#hintModal").modal("show");
			setTimeout(function () {
				$("#hintModal").modal("hide");
				location.reload();
			}, 2000);
		}
	)
}
