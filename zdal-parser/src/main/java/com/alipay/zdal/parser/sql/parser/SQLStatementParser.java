/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.parser.sql.parser;

import java.util.ArrayList;
import java.util.List;

import com.alipay.zdal.parser.sql.ast.SQLExpr;
import com.alipay.zdal.parser.sql.ast.SQLName;
import com.alipay.zdal.parser.sql.ast.SQLStatement;
import com.alipay.zdal.parser.sql.ast.expr.SQLQueryExpr;
import com.alipay.zdal.parser.sql.ast.statement.SQLAssignItem;
import com.alipay.zdal.parser.sql.ast.statement.SQLCallStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLCommentStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLCreateDatabaseStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLCreateTableStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLCreateViewStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLDeleteStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLDropIndexStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLDropTableStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLDropViewStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLExprTableSource;
import com.alipay.zdal.parser.sql.ast.statement.SQLInsertInto;
import com.alipay.zdal.parser.sql.ast.statement.SQLInsertStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLReleaseSavePointStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLRollbackStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLSavePointStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLSelectStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLSetStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLTableSource;
import com.alipay.zdal.parser.sql.ast.statement.SQLTruncateStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLUpdateSetItem;
import com.alipay.zdal.parser.sql.ast.statement.SQLUpdateStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLUseStatement;

/**
 * 
 * @author 伯牙
 * @version $Id: SQLStatementParser.java, v 0.1 2012-11-17 下午3:54:46 Exp $
 */
public class SQLStatementParser extends SQLParser {

    protected SQLExprParser exprParser;

    public SQLStatementParser(String sql) {
        this(new SQLExprParser(sql));
    }

    public SQLStatementParser(SQLExprParser exprParser) {
        super(exprParser.getLexer());
        this.exprParser = exprParser;
    }

    public SQLExprParser getExprParser() {
        return exprParser;
    }

    public List<SQLStatement> parseStatementList() throws ParserException {
        List<SQLStatement> statementList = new ArrayList<SQLStatement>();
        parseStatementList(statementList);
        return statementList;
    }

    public void parseStatementList(List<SQLStatement> statementList) throws ParserException {
        for (;;) {
            if (lexer.token() == Token.EOF) {
                return;
            }

            if (lexer.token() == (Token.SEMI)) {
                lexer.nextToken();
                continue;
            }

            if (lexer.token() == (Token.SELECT)) {
                statementList.add(parseSelect());
                continue;
            }

            if (lexer.token() == (Token.UPDATE)) {
                statementList.add(parseUpdateStatement());
                continue;
            }

            if (lexer.token() == (Token.CREATE)) {
                statementList.add(parseCreate());
                continue;
            }

            if (lexer.token() == (Token.INSERT)) {
                SQLStatement insertStatement = parseInsert();
                statementList.add(insertStatement);

                continue;
            }

            if (lexer.token() == (Token.DELETE)) {
                statementList.add(parseDeleteStatement());
                continue;
            }

            if (lexer.token() == Token.SET) {
                statementList.add(parseSet());
                continue;
            }

            if (lexer.token() == Token.ALTER) {
                statementList.add(parseAlter());
                continue;
            }

            if (lexer.token() == Token.DROP) {
                lexer.nextToken();

                if (lexer.token() == Token.TABLE || identifierEquals("TEMPORARY")) {

                    SQLDropTableStatement stmt = parseDropTable(false);

                    statementList.add(stmt);
                    continue;
                } else if (identifierEquals("USER")) {
                    SQLStatement stmt = parseDropUser();
                    statementList.add(stmt);
                    continue;
                } else if (lexer.token() == Token.INDEX) {
                    SQLStatement stmt = parseDropIndex();
                    statementList.add(stmt);
                    continue;
                } else if (lexer.token() == Token.VIEW) {
                    SQLStatement stmt = parseDropView(false);
                    statementList.add(stmt);
                    continue;
                } else {
                    throw new ParserException("TODO " + lexer.token());
                }
            }

            if (lexer.token() == Token.TRUNCATE) {
                SQLStatement stmt = parseTruncate();
                statementList.add(stmt);
                continue;
            }

            if (lexer.token() == Token.USE) {
                SQLStatement stmt = parseUse();
                statementList.add(stmt);
                continue;
            }

            if (identifierEquals("CALL")) {
                SQLCallStatement stmt = parseCall();
                statementList.add(stmt);
                continue;
            }

            if (identifierEquals("RENAME")) {
                SQLStatement stmt = parseRename();
                statementList.add(stmt);
                continue;
            }

            if (identifierEquals("RELEASE")) {
                SQLStatement stmt = parseReleaseSavePoint();
                statementList.add(stmt);
                continue;
            }

            if (identifierEquals("SAVEPOINT")) {
                SQLStatement stmt = parseSavePoint();
                statementList.add(stmt);
                continue;
            }

            if (identifierEquals("ROLLBACK")) {
                SQLRollbackStatement stmt = parseRollback();

                statementList.add(stmt);
                continue;
            }

            if (identifierEquals("COMMIT")) {
                SQLStatement stmt = parseCommit();

                statementList.add(stmt);
                continue;
            }

            if (identifierEquals("SHOW")) {
                SQLStatement stmt = parseShow();

                statementList.add(stmt);
                continue;
            }

            if (lexer.token() == Token.LPAREN) {
                char mark_ch = lexer.current();
                int mark_bp = lexer.bp();
                lexer.nextToken();
                if (lexer.token() == Token.SELECT) {
                    lexer.reset(mark_bp, mark_ch, Token.LPAREN);
                    SQLStatement stmt = parseSelect();
                    statementList.add(stmt);
                    continue;
                }
            }

            if (parseStatementListDialect(statementList)) {
                continue;
            }

            if (lexer.token() == Token.HINT) {
                lexer.nextToken();
                continue;
            }

            throw new ParserException("TODO " + lexer.token() + " " + lexer.stringVal());
        }
    }

    public SQLRollbackStatement parseRollback() {
        lexer.nextToken();

        if (identifierEquals("WORK")) {
            lexer.nextToken();
        }

        SQLRollbackStatement stmt = new SQLRollbackStatement();

        if (identifierEquals("TO")) {
            lexer.nextToken();

            if (identifierEquals("SAVEPOINT")) {
                lexer.nextToken();
            }

            stmt.setTo(this.exprParser.name());
        }
        return stmt;
    }

    public SQLStatement parseCommit() {
        throw new ParserException("TODO " + lexer.token() + " " + lexer.stringVal());
    }

    public SQLStatement parseShow() {
        throw new ParserException("TODO " + lexer.token() + " " + lexer.stringVal());
    }

    public SQLUseStatement parseUse() {
        accept(Token.USE);
        SQLUseStatement stmt = new SQLUseStatement();
        stmt.setDatabase(this.exprParser.name());
        return stmt;
    }

    public SQLStatement parseSavePoint() {
        acceptIdentifier("SAVEPOINT");
        SQLSavePointStatement stmt = new SQLSavePointStatement();
        stmt.setName(this.exprParser.name());
        return stmt;
    }

    public SQLStatement parseReleaseSavePoint() {
        acceptIdentifier("RELEASE");
        acceptIdentifier("SAVEPOINT");
        SQLReleaseSavePointStatement stmt = new SQLReleaseSavePointStatement();
        stmt.setName(this.exprParser.name());
        return stmt;
    }

    public SQLStatement parseAlter() {
        accept(Token.ALTER);
        throw new ParserException("TODO " + lexer.token() + " " + lexer.stringVal());
    }

    public SQLStatement parseRename() {
        throw new ParserException("TODO " + lexer.token() + " " + lexer.stringVal());
    }

    protected SQLDropTableStatement parseDropTable(boolean acceptDrop) {
        if (acceptDrop) {
            accept(Token.DROP);
        }
        accept(Token.TABLE);

        SQLDropTableStatement stmt = new SQLDropTableStatement();

        for (;;) {
            SQLName name = this.exprParser.name();
            stmt.getTableSources().add(new SQLExprTableSource(name));
            if (lexer.token() == Token.COMMA) {
                lexer.nextToken();
                continue;
            }
            break;
        }
        return stmt;
    }

    protected SQLDropViewStatement parseDropView(boolean acceptDrop) {
        if (acceptDrop) {
            accept(Token.DROP);
        }
        accept(Token.VIEW);

        SQLDropViewStatement stmt = new SQLDropViewStatement();

        for (;;) {
            SQLName name = this.exprParser.name();
            stmt.getTableSources().add(new SQLExprTableSource(name));
            if (lexer.token() == Token.COMMA) {
                lexer.nextToken();
                continue;
            }
            break;
        }
        return stmt;
    }

    public SQLStatement parseTruncate() {
        accept(Token.TRUNCATE);
        if (lexer.token() == Token.TABLE) {
            lexer.nextToken();
        }
        SQLTruncateStatement stmt = new SQLTruncateStatement();

        for (;;) {
            SQLName name = this.exprParser.name();
            stmt.addTableSource(name);

            if (lexer.token() == Token.COMMA) {
                lexer.nextToken();
                continue;
            }

            break;
        }

        return stmt;
    }

    public SQLStatement parseInsert() {
        SQLInsertStatement insertStatement = new SQLInsertStatement();

        if (lexer.token() == Token.INSERT) {
            accept(Token.INSERT);
        }

        parseInsert0(insertStatement);
        return insertStatement;
    }

    protected void parseInsert0(SQLInsertInto insertStatement) {
        parseInsert0(insertStatement, true);
    }

    protected void parseInsert0Hinits(SQLInsertInto insertStatement) {

    }

    protected void parseInsert0(SQLInsertInto insertStatement, boolean acceptSubQuery) {
        if (lexer.token() == Token.INTO) {
            lexer.nextToken();

            SQLName tableName = this.exprParser.name();
            insertStatement.setTableName(tableName);

            if (lexer.token() == Token.LITERAL_ALIAS) {
                insertStatement.setAlias(as());
            }

            parseInsert0Hinits(insertStatement);

            if (lexer.token() == Token.IDENTIFIER) {
                insertStatement.setAlias(lexer.stringVal());
                lexer.nextToken();
            }
        }

        if (lexer.token() == (Token.LPAREN)) {
            lexer.nextToken();
            this.exprParser.exprList(insertStatement.getColumns());
            accept(Token.RPAREN);
        }

        if (lexer.token() == Token.VALUES) {
            lexer.nextToken();
            accept(Token.LPAREN);
            SQLInsertStatement.ValuesClause values = new SQLInsertStatement.ValuesClause();
            this.exprParser.exprList(values.getValues());
            insertStatement.setValues(values);
            accept(Token.RPAREN);
        } else if (acceptSubQuery
                   && (lexer.token() == Token.SELECT || lexer.token() == Token.LPAREN)) {
            SQLQueryExpr queryExpr = (SQLQueryExpr) this.exprParser.expr();
            insertStatement.setQuery(queryExpr.getSubQuery());
        }
    }

    public boolean parseStatementListDialect(List<SQLStatement> statementList) {
        return false;
    }

    public SQLStatement parseDropUser() throws ParserException {
        throw new ParserException("TODO " + lexer.token());
    }

    public SQLStatement parseDropIndex() throws ParserException {
        accept(Token.INDEX);
        SQLDropIndexStatement stmt = new SQLDropIndexStatement();
        stmt.setIndexName(this.exprParser.name());
        accept(Token.ON);
        stmt.setTableName(this.exprParser.name());
        return stmt;
    }

    public SQLCallStatement parseCall() throws ParserException {
        acceptIdentifier("CALL");

        SQLCallStatement stmt = new SQLCallStatement();
        stmt.setProcedureName(exprParser.name());

        if (lexer.token() == Token.LPAREN) {
            lexer.nextToken();
            exprParser.exprList(stmt.getParameters());
            accept(Token.RPAREN);
        }

        return stmt;
    }

    public SQLStatement parseSet() throws ParserException {
        accept(Token.SET);
        SQLSetStatement stmt = new SQLSetStatement();

        parseAssignItems(stmt.getItems());

        return stmt;
    }

    public void parseAssignItems(List<SQLAssignItem> items) {
        for (;;) {
            SQLAssignItem item = exprParser.parseAssignItem();
            items.add(item);

            if (lexer.token() == Token.COMMA) {
                lexer.nextToken();
                continue;
            } else {
                break;
            }
        }
    }

    public SQLStatement parseCreate() throws ParserException {
        char mark_ch = lexer.current();
        int mark_bp = lexer.bp();

        accept(Token.CREATE);

        Token token = lexer.token();

        if (token == Token.TABLE || identifierEquals("GLOBAL")) {
            SQLCreateTableParser createTableParser = getSQLCreateTableParser();
            return createTableParser.parseCrateTable(false);
        } else if (token == Token.INDEX || token == Token.UNIQUE) {
            return parseCreateIndex(false);
        } else if (identifierEquals("SEQUENCE")) {
            return parseCreateSequence(false);
        } else if (token == Token.OR) {
            lexer.nextToken();
            acceptIdentifier("REPLACE");
            if (lexer.token() == Token.PROCEDURE) {
                lexer.reset(mark_bp, mark_ch, Token.CREATE);
                return parseCreateProcedure();
            }

            // lexer.reset(mark_bp, mark_ch, Token.CREATE);
            throw new ParserException("TODO " + lexer.token() + " " + lexer.stringVal());
        } else if (token == Token.DATABASE) {
            return parseCreateDatabase();
        }

        throw new ParserException("TODO " + lexer.token());
    }

    public SQLStatement parseCreateDatabase() {
        if (lexer.token() == Token.CREATE) {
            lexer.nextToken();
        }

        accept(Token.DATABASE);

        SQLCreateDatabaseStatement stmt = new SQLCreateDatabaseStatement();
        stmt.setName(this.exprParser.name());
        return stmt;
    }

    public SQLStatement parseCreateProcedure() {
        throw new ParserException("TODO " + lexer.token());
    }

    public SQLStatement parseCreateSequence(boolean acceptCreate) {
        throw new ParserException("TODO " + lexer.token());
    }

    public SQLStatement parseCreateIndex(boolean acceptCreate) {
        throw new ParserException("TODO " + lexer.token());
    }

    public SQLCreateTableParser getSQLCreateTableParser() {
        return new SQLCreateTableParser(this.exprParser);
    }

    public SQLSelectStatement parseSelect() throws ParserException {
        return new SQLSelectStatement(createSQLSelectParser().select());
    }

    public SQLSelectParser createSQLSelectParser() {
        return new SQLSelectParser(this.exprParser);
    }

    public SQLUpdateStatement parseUpdateStatement() throws ParserException {
        SQLUpdateStatement udpateStatement = createUpdateStatement();

        if (lexer.token() == Token.UPDATE) {
            lexer.nextToken();

            SQLTableSource tableSource = this.exprParser.createSelectParser().parseTableSource();
            udpateStatement.setTableSource(tableSource);
        }

        accept(Token.SET);

        for (;;) {
            SQLUpdateSetItem item = new SQLUpdateSetItem();
            item.setColumn(this.exprParser.name());
            accept(Token.EQ);
            item.setValue(this.exprParser.expr());

            udpateStatement.getItems().add(item);

            if (lexer.token() == (Token.COMMA)) {
                lexer.nextToken();
                continue;
            }

            break;
        }

        if (lexer.token() == (Token.WHERE)) {
            lexer.nextToken();
            udpateStatement.setWhere(this.exprParser.expr());
        }

        return udpateStatement;
    }

    protected SQLUpdateStatement createUpdateStatement() {
        return new SQLUpdateStatement();
    }

    public SQLDeleteStatement parseDeleteStatement() throws ParserException {
        SQLDeleteStatement deleteStatement = new SQLDeleteStatement();

        if (lexer.token() == Token.DELETE) {
            lexer.nextToken();
            if (lexer.token() == (Token.FROM)) {
                lexer.nextToken();
            }

            SQLName tableName = exprParser.name();

            deleteStatement.setTableName(tableName);
        }

        if (lexer.token() == (Token.WHERE)) {
            lexer.nextToken();
            SQLExpr where = this.exprParser.expr();
            deleteStatement.setWhere(where);
        }

        return deleteStatement;
    }

    public SQLCreateTableStatement parseCreateTable() throws ParserException {
        // SQLCreateTableParser parser = new SQLCreateTableParser(this.lexer);
        // return parser.parseCrateTable();
        throw new ParserException("TODO");
    }

    public SQLCreateViewStatement parseCreateView() throws ParserException {
        SQLCreateViewStatement createView = new SQLCreateViewStatement();

        this.accept(Token.CREATE);

        this.accept(Token.VIEW);

        createView.setName(exprParser.name());

        if (lexer.token() == Token.LPAREN) {
            lexer.nextToken();
            this.exprParser.exprList(createView.getColumns());
            accept(Token.RPAREN);
        }

        this.accept(Token.AS);

        createView.setSubQuery(new SQLSelectParser(this.exprParser).select());
        return createView;
    }

    public SQLCommentStatement parseComment() {
        accept(Token.COMMENT);
        SQLCommentStatement stmt = new SQLCommentStatement();

        accept(Token.ON);

        if (lexer.token() == Token.TABLE) {
            stmt.setType(SQLCommentStatement.Type.TABLE);
            lexer.nextToken();
        } else if (lexer.token() == Token.COLUMN) {
            stmt.setType(SQLCommentStatement.Type.COLUMN);
            lexer.nextToken();
        }

        stmt.setOn(this.exprParser.name());

        accept(Token.IS);
        stmt.setComment(this.exprParser.expr());

        return stmt;
    }
}
