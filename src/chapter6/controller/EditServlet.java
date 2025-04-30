package chapter6.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import chapter6.beans.Message;
import chapter6.beans.User;
import chapter6.logging.InitApplication;
import chapter6.service.MessageService;

@WebServlet(urlPatterns = { "/edit" })
public class EditServlet extends HttpServlet {

	/**
	* ロガーインスタンスの生成
	*/
	Logger log = Logger.getLogger("twitter");

	/**
	* デフォルトコンストラクタ
	* アプリケーションの初期化を実施する。
	*/
	public EditServlet() {
		InitApplication application = InitApplication.getInstance();
		application.init();

	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		log.info(new Object() {
		}.getClass().getEnclosingClass().getName() +
				" : " + new Object() {
				}.getClass().getEnclosingMethod().getName());
		HttpSession session = request.getSession();

		//top.jspからeditidを取得
		String strEditId = request.getParameter("editId");

		//loginUserのidをget
		User user = (User) request.getSession().getAttribute("loginUser");
		int intUserId = user.getId();
		//String型に変換
		String userId = Integer.toString(intUserId);

		//変数宣言
		Message message = null;
		String messageUserId = null;
		List<String> errorMessages = new ArrayList<String>();

		//URL=id部分の空白、スペース、数字以外判定
		if (!StringUtils.isBlank(strEditId) && strEditId.matches("^[0-9]+$")) {

			//int型に変換
			int editId = Integer.parseInt(strEditId);
			//編集したいidのmessage内容とuserIdを取得
			message = new MessageService().select(editId);

		}

		//idが存在するか確認
		if (message != null) {
			//存在したらmessageのuser_idをget
			int intMessageUserId = message.getUserId();
			//String型に変換
			messageUserId = Integer.toString(intMessageUserId);

		}

		//コーチに確認しuseridをjspから取得しようとしましたが、
		//打鍵テストの「URLにつぶやきのID以外の情報が含まれていないこと」の
		//条件に違反してしまうためif文をidが存在した場合に処理することにしました。
		//loginuserのidとmessageのuser_idが一致するか確認
		if (!userId.equals(messageUserId)) {

			//エラーメッセージを出力
			errorMessages.add("不正なパラメータが入力されました");
			session.setAttribute("errorMessages", errorMessages);
			response.sendRedirect("./top.jsp");
			return;

		}

		//edit.jspにforwardして返す
		request.setAttribute("message", message);
		request.getRequestDispatcher("edit.jsp").forward(request, response);

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		log.info(new Object() {
		}.getClass().getEnclosingClass().getName() +
				" : " + new Object() {
				}.getClass().getEnclosingMethod().getName());

		HttpSession session = request.getSession();
		List<String> errorMessages = new ArrayList<String>();
		Message editMessage = new Message();

		//jspからtextを取得
		String text = request.getParameter("text");
		editMessage.setText(text);
		//jspからidを取得
		String strId = request.getParameter("editId");
		int id = Integer.parseInt(strId);
		editMessage.setId(id);
		//140文字以下か判定
		if (!isValid(text, errorMessages)) {

			session.setAttribute("errorMessages", errorMessages);
			Message message = new Message();
			message.setText(text);
			session.setAttribute("message", message);
			request.getRequestDispatcher("edit.jsp").forward(request, response);
			return;

		}

		new MessageService().update(editMessage);
		response.sendRedirect("./top.jsp");

	}

	private boolean isValid(String text, List<String> errorMessages) {

		log.info(new Object() {
		}.getClass().getEnclosingClass().getName() +
				" : " + new Object() {
				}.getClass().getEnclosingMethod().getName());

		if (StringUtils.isBlank(text)) {
			errorMessages.add("メッセージを入力してください");
		} else if (140 < text.length()) {
			errorMessages.add("140文字以下で入力してください");
		}

		if (errorMessages.size() != 0) {
			return false;
		}
		return true;
	}
}
