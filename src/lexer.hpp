#ifndef LEXER_H_
#define LEXER_H_

#include <iostream>
#include <cstring>
#include "token.hpp"

namespace Compiler
{
    using std::string;
    class Lexer
    {
    private:
        string token;

    public:
        Lexer();
        ~Lexer();
        string get_token(FILE *file);
        TokenType get_token_type();
    };

    Lexer::Lexer()
    {
    }

    Lexer::~Lexer()
    {
    }
}

#endif