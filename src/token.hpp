#ifndef TOKEN_H_
#define TOKEN_H_

enum class TokenType
{
    TOKEN_EOF = 0,
    TOKEN_NUMBER,
    TOKEN_INT,
    TOKEN_MAIN,
    TOKEN_RETURN,
    TOKEN_PAREN_L,
    TOKEN_PAREN_R,
    TOKEN_BRACE_L,
    TOKEN_BRACE_R,
    TOKEN_SEMICOLON,
    TOKEN_PLUS,
    TOKEN_MINUS,
}

#endif