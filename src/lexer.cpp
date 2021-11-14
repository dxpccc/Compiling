#include <string>
#include "lexer.hpp"

Lexer::Lexer(FILE *input)
{
    input_ = input;
}

Lexer::Lexer()
{
    Lexer(nullptr);
}

Token Lexer::get_token()
{
    static char cur_char = ' ';
}

Token Lexer::get_next_token()
{
}

TokenType get_token_type()
{
}

std::string get_token_value()
{
}

std::vector<Token> &Lexer::get_tokens()
{
    return tokens;
}
