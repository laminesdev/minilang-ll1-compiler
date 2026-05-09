# MiniLang Compiler - LL(1) Table-Driven Parser

A complete compiler implementation for a simple programming language using LL(1) table-driven parsing approach.

## Project Overview

This compiler transforms a simple imperative language (MiniLang) into C code. It uses **LL(1) table-driven parsing** instead of traditional recursive descent, making the parsing strategy explicit and verifiable through the parsing table.

## Language Features

- **Data Types**: `int`, `float`
- **Statements**:
  - Variable declarations
  - Assignment statements
  - If-else conditional statements
  - While loops
  - Print statements
  - Block statements (braces)
- **Expressions**:
  - Arithmetic: `+`, `-`, `*`, `/`
  - Relational: `==`, `!=`, `<`, `>`, `<=`, `>=`

## Example

```
int x;
int y;
x = 10;
y = 5;
if (x > y) {
    print(x);
}
while (x > 0) {
    x = x - 1;
}
```

## Project Structure

```
simple/
├── Lexer.java           # Lexical analyzer
├── Token.java           # Token representation
├── TokenType.java       # Token type enumeration
├── Grammar.java         # Grammar definition (LL(1))
├── FirstAndFollow.java  # First & Follow sets computation
├── ParseTable.java      # LL(1) parsing table builder
├── LL1Parser.java       # Table-driven parser
├── SemanticAnalyzer.java # Type checking & symbol table
├── CodeGenerator.java   # C code generator
├── Main.java            # Compiler driver
├── test.mlang            # Test input file
└── legacy/
    └── Parser.java      # Old recursive descent parser
```

## How It Works

### 1. Lexical Analysis (Lexer.java)
Scans the source code and produces a stream of tokens.

### 2. Syntax Analysis (LL1Parser.java)
Uses an LL(1) parsing table to parse the token stream:
- **Grammar** - Defines the language syntax
- **First/Follow** - Computes grammar properties
- **Parse Table** - Maps (non-terminal, terminal) → production

### 3. Semantic Analysis (SemanticAnalyzer.java)
- Type checking
- Symbol table management
- Variable declaration verification

### 4. Code Generation (CodeGenerator.java)
Generates equivalent C code from the AST.

## Building and Running

### Compile
```bash
javac *.java
```

### Run
```bash
java Main test.mlang
```

### Output
- Lexical analysis tokens
- Syntax analysis (LL(1) Table-Driven) ✓
- Semantic analysis with symbol table
- Generated C code in `test.c`

## LL(1) Table-Driven Parsing

### The Parsing Table

The core of the approach is the parse table:

| Non-Terminal | INT | IDENTIFIER | IF | WHILE | ... |
|--------------|-----|------------|----|-------|-----|
| Program      | P1  | P1         | P1 | P1    | ... |
| Statement    | -   | P2         | P3 | P4    | ... |
| Declaration  | P5  | -          | -  | -     | ... |

**P1** = `Program → DeclarationList StatementList`

### Parsing Algorithm

```
Stack: [$ Program]     Input: int x; y = 5; ...
1. Look up: M["Program", "INT"] = Production
2. Pop "Program", push production RHS in reverse
3. Continue until stack and input are both "$"
```

### Key Difference from Recursive Descent

| Recursive Descent | LL(1) Table-Driven |
|-------------------|-------------------|
| Logic in code (`if (check(INT))`) | Logic in table (`table[nonTerminal][terminal]`) |
| Hard to verify | Table can be inspected/verified |
| Tied to method structure | Decoupled from implementation |

## Files Description

### Core LL(1) Components

- **Grammar.java**: Formal grammar with productions, terminals, non-terminals
- **FirstAndFollow.java**: Computes First and Follow sets
- **ParseTable.java**: Builds the LL(1) parsing table
- **LL1Parser.java**: Table-driven parser that builds AST

### Supporting Files

- **Lexer.java**: Tokenizer/scanner
- **SemanticAnalyzer.java**: Type and symbol checking
- **CodeGenerator.java**: C code output
- **Main.java**: Entry point and phase orchestration

## License

Educational project for compiler construction course.