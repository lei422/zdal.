/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.parser.sql.dialect.oracle.ast.expr;

import com.alipay.zdal.parser.sql.ast.SQLExprImpl;
import com.alipay.zdal.parser.sql.ast.expr.SQLLiteralExpr;
import com.alipay.zdal.parser.sql.dialect.oracle.visitor.OracleASTVisitor;
import com.alipay.zdal.parser.sql.visitor.SQLASTVisitor;

/**
 * 
 * @author 伯牙
 * @version $Id: OracleDateExpr.java, v 0.1 2012-11-17 下午3:43:24 Exp $
 */
public class OracleDateExpr extends SQLExprImpl implements SQLLiteralExpr, OracleExpr {

    private static final long serialVersionUID = 1L;

    private String            literal;

    public OracleDateExpr() {

    }

    public String getLiteral() {
        return literal;
    }

    public void setLiteral(String literal) {
        this.literal = literal;
    }

    @Override
    protected void accept0(SQLASTVisitor visitor) {
        this.accept0((OracleASTVisitor) visitor);
    }

    public void accept0(OracleASTVisitor visitor) {
        visitor.visit(this);
        visitor.endVisit(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((literal == null) ? 0 : literal.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        OracleDateExpr other = (OracleDateExpr) obj;
        if (literal == null) {
            if (other.literal != null) {
                return false;
            }
        } else if (!literal.equals(other.literal)) {
            return false;
        }
        return true;
    }

}
