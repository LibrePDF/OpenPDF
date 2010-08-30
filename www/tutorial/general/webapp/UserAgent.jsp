<%
  String user = request.getHeader("User-Agent");
  if(user.indexOf("MSIE") != -1 && user.indexOf("Windows") != -1){
    out.print("<body leftMargin=\"0\" topMargin=\"0\" scroll=\"no\">");
    out.print("<EMBED src=\"/servlet/simple.pdf?msg="
    	+ user
    	+ "\" width=\"100%\" height=\"100%\"  fullscreen=\"yes\" type=\"application/pdf\">");
  }
  else{
    response.sendRedirect("/servlet/simple.pdf?msg=" + user);
  }
%>