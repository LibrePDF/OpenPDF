<html>
	<head>
		<title>A form for OutSimplePdf: GET or POST</title>
	</head>
	<body>
<%
	String method = request.getParameter("method");
	if (method == null) method = "GET";
%>
		The action of this form is <%= method %>
		<form action="/servlet/simple.pdf" method="<%= method%>">
			<textarea name="msg" cols="50" rows="20">Write some text you want to see in PDF.</textarea><br>
			Click to see PDF: <input type="Submit" value="GeneratePDF">
		</form>
	</body>
</html>