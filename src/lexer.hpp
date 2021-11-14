#ifndef LEXER_H_
#define LEXER_H_

#include "token.hpp"
#include <string>
#include <vector>

struct Token
{
    TokenType type;
    std::string value;
};

class Lexer
{
private:
    FILE *input_;
    std::vector<Token> tokens;
    Token get_token();
    Token get_next_token();
    TokenType get_token_type();
    std::string get_token_value();

public:
    Lexer();
    Lexer(FILE *input);
    ~Lexer();
    bool run();
    std::vector<Token> &get_tokens();
};

#endif