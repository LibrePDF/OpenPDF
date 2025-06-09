/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.css.parser;

%%

%class Lexer

%unicode
%ignorecase 
%line
%type Token

%{
    public int yyline() {
    	return this.yyline;
    }
    
    public void setyyline(int i) {
    	this.yyline = i;
	}
%}

h		      = [0-9a-fA-F]
nonascii      = [^\000-\177]
unicode       = \\{h}{1,6}(\r\n|[ \t\r\n\f])?
escape        = {unicode}|\\[^\r\n\f0-9a-fA-F]
nmstart       = [_a-zA-Z]|{nonascii}|{escape}
nmchar        = [_a-zA-Z0-9-]|{nonascii}|{escape}
string1       = \"([^\n\r\f\"\\]|\\{nl}|{escape})*\"
string2       = \'([^\n\r\f\'\\]|\\{nl}|{escape})*\'
invalid1      = \"([^\n\r\f\"\\]|\\{nl}|{escape})*
invalid2      = \'([^\n\r\f\'\\]|\\{nl}|{escape})*


comment       = \/\*[^*]*\*+([^/*][^*]*\*+)*\/
ident         = -?{nmstart}{nmchar}*
name          = {nmchar}+
num           = [0-9]+|[0-9]*"."[0-9]+
string        = {string1}|{string2}
invalid       = {invalid1}|{invalid2}
url           = ([!#$%&*-~]|{nonascii}|{escape})*
s             = [ \t\r\n\f]+
w             = {s}?
nl            = \n|\r\n|\r|\f

A             = a|\\0{0,4}(41|61)(\r\n|[ \t\r\n\f])?
C             = c|\\0{0,4}(43|63)(\r\n|[ \t\r\n\f])?
D             = d|\\0{0,4}(44|64)(\r\n|[ \t\r\n\f])?
E             = e|\\0{0,4}(45|65)(\r\n|[ \t\r\n\f])?
G             = g|\\0{0,4}(47|67)(\r\n|[ \t\r\n\f])?|\\g
H             = h|\\0{0,4}(48|68)(\r\n|[ \t\r\n\f])?|\\h
I             = i|\\0{0,4}(49|69)(\r\n|[ \t\r\n\f])?|\\i
K             = k|\\0{0,4}(4b|6b)(\r\n|[ \t\r\n\f])?|\\k
M             = m|\\0{0,4}(4d|6d)(\r\n|[ \t\r\n\f])?|\\m
N             = n|\\0{0,4}(4e|6e)(\r\n|[ \t\r\n\f])?|\\n
P             = p|\\0{0,4}(50|70)(\r\n|[ \t\r\n\f])?|\\p
R             = r|\\0{0,4}(52|72)(\r\n|[ \t\r\n\f])?|\\r
S             = s|\\0{0,4}(53|73)(\r\n|[ \t\r\n\f])?|\\s
T             = t|\\0{0,4}(54|74)(\r\n|[ \t\r\n\f])?|\\t
X             = x|\\0{0,4}(58|78)(\r\n|[ \t\r\n\f])?|\\x
Z             = z|\\0{0,4}(5a|7a)(\r\n|[ \t\r\n\f])?|\\z

%%

{s}			{return Token.TK_S;}

{comment}	{ /* ignore comments */ }

"<!--"			{return Token.TK_CDO;}
"-->"			{return Token.TK_CDC;}
"~="			{return Token.TK_INCLUDES;}
"|="			{return Token.TK_DASHMATCH;}
"^="            {return Token.TK_PREFIXMATCH;}
"$="            {return Token.TK_SUFFIXMATCH;}
"*="            {return Token.TK_SUBSTRINGMATCH;}

{w}"{"			{return Token.TK_LBRACE;}
{w}"+"			{return Token.TK_PLUS;}
{w}">"			{return Token.TK_GREATER;}
{w}","			{return Token.TK_COMMA;}

{string}		{return Token.TK_STRING;}
{invalid}		{return Token.TK_INVALID; /* unclosed string */}

{ident}			{return Token.TK_IDENT;}

"#"{name}		{return Token.TK_HASH;}

"@import"		{return Token.TK_IMPORT_SYM;}
"@page"			{return Token.TK_PAGE_SYM;}
"@media"		{return Token.TK_MEDIA_SYM;}
"@charset "		{return Token.TK_CHARSET_SYM;}
"@namespace"	{return Token.TK_NAMESPACE_SYM;}
"@font-face"    {return Token.TK_FONT_FACE_SYM;}
"@"{ident}      {return Token.TK_AT_RULE;}

"!"({w}|{comment})*"important"	{return Token.TK_IMPORTANT_SYM;}

{num}{E}{M}		{return Token.TK_EMS;}
{num}{E}{X}		{return Token.TK_EXS;}
{num}{P}{X}		{return Token.TK_PX;}
{num}{C}{M}		{return Token.TK_CM;}
{num}{M}{M}		{return Token.TK_MM;}
{num}{I}{N}		{return Token.TK_IN;}
{num}{P}{T}		{return Token.TK_PT;}
{num}{P}{C}		{return Token.TK_PC;}
{num}{D}{E}{G}		{return Token.TK_ANGLE;}
{num}{R}{A}{D}		{return Token.TK_ANGLE;}
{num}{G}{R}{A}{D}	{return Token.TK_ANGLE;}
{num}{M}{S}		{return Token.TK_TIME;}
{num}{S}		{return Token.TK_TIME;}
{num}{H}{Z}		{return Token.TK_FREQ;}
{num}{K}{H}{Z}		{return Token.TK_FREQ;}
{num}{ident}		{return Token.TK_DIMENSION;}

{num}%			{return Token.TK_PERCENTAGE;}
{num}			{return Token.TK_NUMBER;}

"url("{w}{string}{w}")"	{return Token.TK_URI;}
"url("{w}{url}{w}")"	{return Token.TK_URI;}
{ident}"("		{return Token.TK_FUNCTION;}

"}"				{return Token.TK_RBRACE;}
";"				{return Token.TK_SEMICOLON;}
"/"             {return Token.TK_VIRGULE;} 
":"             {return Token.TK_COLON;} 
"-"             {return Token.TK_MINUS;}
")"             {return Token.TK_RPAREN;} 
"["				{return Token.TK_LBRACKET;}
"]"				{return Token.TK_RBRACKET;}
"."				{return Token.TK_PERIOD;}
"="				{return Token.TK_EQUALS;}
"*"				{return Token.TK_ASTERISK;}
"|"				{return Token.TK_VERTICAL_BAR;}

<<EOF>>			{return Token.TK_EOF;}

.				{return Token.createOtherToken(yytext());}

