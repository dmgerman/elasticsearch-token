begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.painless
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|painless
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Scorer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|lookup
operator|.
name|LeafDocLookup
import|;
end_import

begin_import
import|import
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Handle
import|;
end_import

begin_import
import|import
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Opcodes
import|;
end_import

begin_import
import|import
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Type
import|;
end_import

begin_import
import|import
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|commons
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|invoke
operator|.
name|CallSite
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|invoke
operator|.
name|MethodHandles
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|invoke
operator|.
name|MethodType
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_class
DECL|class|WriterConstants
class|class
name|WriterConstants
block|{
DECL|field|BASE_CLASS_NAME
specifier|final
specifier|static
name|String
name|BASE_CLASS_NAME
init|=
name|Executable
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
DECL|field|CLASS_NAME
specifier|final
specifier|static
name|String
name|CLASS_NAME
init|=
name|BASE_CLASS_NAME
operator|+
literal|"$CompiledPainlessExecutable"
decl_stmt|;
DECL|field|BASE_CLASS_TYPE
specifier|final
specifier|static
name|Type
name|BASE_CLASS_TYPE
init|=
name|Type
operator|.
name|getType
argument_list|(
name|Executable
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|CLASS_TYPE
specifier|final
specifier|static
name|Type
name|CLASS_TYPE
init|=
name|Type
operator|.
name|getType
argument_list|(
literal|"L"
operator|+
name|CLASS_NAME
operator|.
name|replace
argument_list|(
literal|"."
argument_list|,
literal|"/"
argument_list|)
operator|+
literal|";"
argument_list|)
decl_stmt|;
DECL|field|CONSTRUCTOR
specifier|final
specifier|static
name|Method
name|CONSTRUCTOR
init|=
name|getAsmMethod
argument_list|(
name|void
operator|.
name|class
argument_list|,
literal|"<init>"
argument_list|,
name|Definition
operator|.
name|class
argument_list|,
name|String
operator|.
name|class
argument_list|,
name|String
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|EXECUTE
specifier|final
specifier|static
name|Method
name|EXECUTE
init|=
name|getAsmMethod
argument_list|(
name|Object
operator|.
name|class
argument_list|,
literal|"execute"
argument_list|,
name|Map
operator|.
name|class
argument_list|,
name|Scorer
operator|.
name|class
argument_list|,
name|LeafDocLookup
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|PAINLESS_ERROR_TYPE
specifier|final
specifier|static
name|Type
name|PAINLESS_ERROR_TYPE
init|=
name|Type
operator|.
name|getType
argument_list|(
name|PainlessError
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEFINITION_TYPE
specifier|final
specifier|static
name|Type
name|DEFINITION_TYPE
init|=
name|Type
operator|.
name|getType
argument_list|(
name|Definition
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|NEEDS_SCORE_TYPE
specifier|final
specifier|static
name|Type
name|NEEDS_SCORE_TYPE
init|=
name|Type
operator|.
name|getType
argument_list|(
name|NeedsScore
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|OBJECT_TYPE
specifier|final
specifier|static
name|Type
name|OBJECT_TYPE
init|=
name|Type
operator|.
name|getType
argument_list|(
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|SCORER_TYPE
specifier|final
specifier|static
name|Type
name|SCORER_TYPE
init|=
name|Type
operator|.
name|getType
argument_list|(
name|Scorer
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|SCORER_SCORE
specifier|final
specifier|static
name|Method
name|SCORER_SCORE
init|=
name|getAsmMethod
argument_list|(
name|float
operator|.
name|class
argument_list|,
literal|"score"
argument_list|)
decl_stmt|;
DECL|field|MAP_TYPE
specifier|final
specifier|static
name|Type
name|MAP_TYPE
init|=
name|Type
operator|.
name|getType
argument_list|(
name|Map
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|MAP_GET
specifier|final
specifier|static
name|Method
name|MAP_GET
init|=
name|getAsmMethod
argument_list|(
name|Object
operator|.
name|class
argument_list|,
literal|"get"
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** dynamic callsite bootstrap signature */
DECL|field|DEF_BOOTSTRAP_TYPE
specifier|final
specifier|static
name|MethodType
name|DEF_BOOTSTRAP_TYPE
init|=
name|MethodType
operator|.
name|methodType
argument_list|(
name|CallSite
operator|.
name|class
argument_list|,
name|MethodHandles
operator|.
name|Lookup
operator|.
name|class
argument_list|,
name|String
operator|.
name|class
argument_list|,
name|MethodType
operator|.
name|class
argument_list|,
name|int
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_BOOTSTRAP_HANDLE
specifier|final
specifier|static
name|Handle
name|DEF_BOOTSTRAP_HANDLE
init|=
operator|new
name|Handle
argument_list|(
name|Opcodes
operator|.
name|H_INVOKESTATIC
argument_list|,
name|Type
operator|.
name|getInternalName
argument_list|(
name|DynamicCallSite
operator|.
name|class
argument_list|)
argument_list|,
literal|"bootstrap"
argument_list|,
name|WriterConstants
operator|.
name|DEF_BOOTSTRAP_TYPE
operator|.
name|toMethodDescriptorString
argument_list|()
argument_list|)
decl_stmt|;
DECL|field|DEF_DYNAMIC_LOAD_FIELD_DESC
specifier|final
specifier|static
name|String
name|DEF_DYNAMIC_LOAD_FIELD_DESC
init|=
name|MethodType
operator|.
name|methodType
argument_list|(
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
operator|.
name|toMethodDescriptorString
argument_list|()
decl_stmt|;
DECL|field|DEF_DYNAMIC_STORE_FIELD_DESC
specifier|final
specifier|static
name|String
name|DEF_DYNAMIC_STORE_FIELD_DESC
init|=
name|MethodType
operator|.
name|methodType
argument_list|(
name|void
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
operator|.
name|toMethodDescriptorString
argument_list|()
decl_stmt|;
DECL|field|DEF_DYNAMIC_ARRAY_LOAD_DESC
specifier|final
specifier|static
name|String
name|DEF_DYNAMIC_ARRAY_LOAD_DESC
init|=
name|MethodType
operator|.
name|methodType
argument_list|(
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
operator|.
name|toMethodDescriptorString
argument_list|()
decl_stmt|;
DECL|field|DEF_DYNAMIC_ARRAY_STORE_DESC
specifier|final
specifier|static
name|String
name|DEF_DYNAMIC_ARRAY_STORE_DESC
init|=
name|MethodType
operator|.
name|methodType
argument_list|(
name|void
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
operator|.
name|toMethodDescriptorString
argument_list|()
decl_stmt|;
DECL|field|DEF_NOT_CALL
specifier|final
specifier|static
name|Method
name|DEF_NOT_CALL
init|=
name|getAsmMethod
argument_list|(
name|Object
operator|.
name|class
argument_list|,
literal|"not"
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_NEG_CALL
specifier|final
specifier|static
name|Method
name|DEF_NEG_CALL
init|=
name|getAsmMethod
argument_list|(
name|Object
operator|.
name|class
argument_list|,
literal|"neg"
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_MUL_CALL
specifier|final
specifier|static
name|Method
name|DEF_MUL_CALL
init|=
name|getAsmMethod
argument_list|(
name|Object
operator|.
name|class
argument_list|,
literal|"mul"
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_DIV_CALL
specifier|final
specifier|static
name|Method
name|DEF_DIV_CALL
init|=
name|getAsmMethod
argument_list|(
name|Object
operator|.
name|class
argument_list|,
literal|"div"
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_REM_CALL
specifier|final
specifier|static
name|Method
name|DEF_REM_CALL
init|=
name|getAsmMethod
argument_list|(
name|Object
operator|.
name|class
argument_list|,
literal|"rem"
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_ADD_CALL
specifier|final
specifier|static
name|Method
name|DEF_ADD_CALL
init|=
name|getAsmMethod
argument_list|(
name|Object
operator|.
name|class
argument_list|,
literal|"add"
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_SUB_CALL
specifier|final
specifier|static
name|Method
name|DEF_SUB_CALL
init|=
name|getAsmMethod
argument_list|(
name|Object
operator|.
name|class
argument_list|,
literal|"sub"
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_LSH_CALL
specifier|final
specifier|static
name|Method
name|DEF_LSH_CALL
init|=
name|getAsmMethod
argument_list|(
name|Object
operator|.
name|class
argument_list|,
literal|"lsh"
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_RSH_CALL
specifier|final
specifier|static
name|Method
name|DEF_RSH_CALL
init|=
name|getAsmMethod
argument_list|(
name|Object
operator|.
name|class
argument_list|,
literal|"rsh"
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_USH_CALL
specifier|final
specifier|static
name|Method
name|DEF_USH_CALL
init|=
name|getAsmMethod
argument_list|(
name|Object
operator|.
name|class
argument_list|,
literal|"ush"
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_AND_CALL
specifier|final
specifier|static
name|Method
name|DEF_AND_CALL
init|=
name|getAsmMethod
argument_list|(
name|Object
operator|.
name|class
argument_list|,
literal|"and"
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_XOR_CALL
specifier|final
specifier|static
name|Method
name|DEF_XOR_CALL
init|=
name|getAsmMethod
argument_list|(
name|Object
operator|.
name|class
argument_list|,
literal|"xor"
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_OR_CALL
specifier|final
specifier|static
name|Method
name|DEF_OR_CALL
init|=
name|getAsmMethod
argument_list|(
name|Object
operator|.
name|class
argument_list|,
literal|"or"
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_EQ_CALL
specifier|final
specifier|static
name|Method
name|DEF_EQ_CALL
init|=
name|getAsmMethod
argument_list|(
name|boolean
operator|.
name|class
argument_list|,
literal|"eq"
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_LT_CALL
specifier|final
specifier|static
name|Method
name|DEF_LT_CALL
init|=
name|getAsmMethod
argument_list|(
name|boolean
operator|.
name|class
argument_list|,
literal|"lt"
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_LTE_CALL
specifier|final
specifier|static
name|Method
name|DEF_LTE_CALL
init|=
name|getAsmMethod
argument_list|(
name|boolean
operator|.
name|class
argument_list|,
literal|"lte"
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_GT_CALL
specifier|final
specifier|static
name|Method
name|DEF_GT_CALL
init|=
name|getAsmMethod
argument_list|(
name|boolean
operator|.
name|class
argument_list|,
literal|"gt"
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_GTE_CALL
specifier|final
specifier|static
name|Method
name|DEF_GTE_CALL
init|=
name|getAsmMethod
argument_list|(
name|boolean
operator|.
name|class
argument_list|,
literal|"gte"
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|STRINGBUILDER_TYPE
specifier|final
specifier|static
name|Type
name|STRINGBUILDER_TYPE
init|=
name|Type
operator|.
name|getType
argument_list|(
name|StringBuilder
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|STRINGBUILDER_CONSTRUCTOR
specifier|final
specifier|static
name|Method
name|STRINGBUILDER_CONSTRUCTOR
init|=
name|getAsmMethod
argument_list|(
name|void
operator|.
name|class
argument_list|,
literal|"<init>"
argument_list|)
decl_stmt|;
DECL|field|STRINGBUILDER_APPEND_BOOLEAN
specifier|final
specifier|static
name|Method
name|STRINGBUILDER_APPEND_BOOLEAN
init|=
name|getAsmMethod
argument_list|(
name|StringBuilder
operator|.
name|class
argument_list|,
literal|"append"
argument_list|,
name|boolean
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|STRINGBUILDER_APPEND_CHAR
specifier|final
specifier|static
name|Method
name|STRINGBUILDER_APPEND_CHAR
init|=
name|getAsmMethod
argument_list|(
name|StringBuilder
operator|.
name|class
argument_list|,
literal|"append"
argument_list|,
name|char
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|STRINGBUILDER_APPEND_INT
specifier|final
specifier|static
name|Method
name|STRINGBUILDER_APPEND_INT
init|=
name|getAsmMethod
argument_list|(
name|StringBuilder
operator|.
name|class
argument_list|,
literal|"append"
argument_list|,
name|int
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|STRINGBUILDER_APPEND_LONG
specifier|final
specifier|static
name|Method
name|STRINGBUILDER_APPEND_LONG
init|=
name|getAsmMethod
argument_list|(
name|StringBuilder
operator|.
name|class
argument_list|,
literal|"append"
argument_list|,
name|long
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|STRINGBUILDER_APPEND_FLOAT
specifier|final
specifier|static
name|Method
name|STRINGBUILDER_APPEND_FLOAT
init|=
name|getAsmMethod
argument_list|(
name|StringBuilder
operator|.
name|class
argument_list|,
literal|"append"
argument_list|,
name|float
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|STRINGBUILDER_APPEND_DOUBLE
specifier|final
specifier|static
name|Method
name|STRINGBUILDER_APPEND_DOUBLE
init|=
name|getAsmMethod
argument_list|(
name|StringBuilder
operator|.
name|class
argument_list|,
literal|"append"
argument_list|,
name|double
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|STRINGBUILDER_APPEND_STRING
specifier|final
specifier|static
name|Method
name|STRINGBUILDER_APPEND_STRING
init|=
name|getAsmMethod
argument_list|(
name|StringBuilder
operator|.
name|class
argument_list|,
literal|"append"
argument_list|,
name|String
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|STRINGBUILDER_APPEND_OBJECT
specifier|final
specifier|static
name|Method
name|STRINGBUILDER_APPEND_OBJECT
init|=
name|getAsmMethod
argument_list|(
name|StringBuilder
operator|.
name|class
argument_list|,
literal|"append"
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|STRINGBUILDER_TOSTRING
specifier|final
specifier|static
name|Method
name|STRINGBUILDER_TOSTRING
init|=
name|getAsmMethod
argument_list|(
name|String
operator|.
name|class
argument_list|,
literal|"toString"
argument_list|)
decl_stmt|;
DECL|field|TOINTEXACT_LONG
specifier|final
specifier|static
name|Method
name|TOINTEXACT_LONG
init|=
name|getAsmMethod
argument_list|(
name|int
operator|.
name|class
argument_list|,
literal|"toIntExact"
argument_list|,
name|long
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|NEGATEEXACT_INT
specifier|final
specifier|static
name|Method
name|NEGATEEXACT_INT
init|=
name|getAsmMethod
argument_list|(
name|int
operator|.
name|class
argument_list|,
literal|"negateExact"
argument_list|,
name|int
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|NEGATEEXACT_LONG
specifier|final
specifier|static
name|Method
name|NEGATEEXACT_LONG
init|=
name|getAsmMethod
argument_list|(
name|long
operator|.
name|class
argument_list|,
literal|"negateExact"
argument_list|,
name|long
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|MULEXACT_INT
specifier|final
specifier|static
name|Method
name|MULEXACT_INT
init|=
name|getAsmMethod
argument_list|(
name|int
operator|.
name|class
argument_list|,
literal|"multiplyExact"
argument_list|,
name|int
operator|.
name|class
argument_list|,
name|int
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|MULEXACT_LONG
specifier|final
specifier|static
name|Method
name|MULEXACT_LONG
init|=
name|getAsmMethod
argument_list|(
name|long
operator|.
name|class
argument_list|,
literal|"multiplyExact"
argument_list|,
name|long
operator|.
name|class
argument_list|,
name|long
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|ADDEXACT_INT
specifier|final
specifier|static
name|Method
name|ADDEXACT_INT
init|=
name|getAsmMethod
argument_list|(
name|int
operator|.
name|class
argument_list|,
literal|"addExact"
argument_list|,
name|int
operator|.
name|class
argument_list|,
name|int
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|ADDEXACT_LONG
specifier|final
specifier|static
name|Method
name|ADDEXACT_LONG
init|=
name|getAsmMethod
argument_list|(
name|long
operator|.
name|class
argument_list|,
literal|"addExact"
argument_list|,
name|long
operator|.
name|class
argument_list|,
name|long
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|SUBEXACT_INT
specifier|final
specifier|static
name|Method
name|SUBEXACT_INT
init|=
name|getAsmMethod
argument_list|(
name|int
operator|.
name|class
argument_list|,
literal|"subtractExact"
argument_list|,
name|int
operator|.
name|class
argument_list|,
name|int
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|SUBEXACT_LONG
specifier|final
specifier|static
name|Method
name|SUBEXACT_LONG
init|=
name|getAsmMethod
argument_list|(
name|long
operator|.
name|class
argument_list|,
literal|"subtractExact"
argument_list|,
name|long
operator|.
name|class
argument_list|,
name|long
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|CHECKEQUALS
specifier|final
specifier|static
name|Method
name|CHECKEQUALS
init|=
name|getAsmMethod
argument_list|(
name|boolean
operator|.
name|class
argument_list|,
literal|"checkEquals"
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|TOBYTEEXACT_INT
specifier|final
specifier|static
name|Method
name|TOBYTEEXACT_INT
init|=
name|getAsmMethod
argument_list|(
name|byte
operator|.
name|class
argument_list|,
literal|"toByteExact"
argument_list|,
name|int
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|TOBYTEEXACT_LONG
specifier|final
specifier|static
name|Method
name|TOBYTEEXACT_LONG
init|=
name|getAsmMethod
argument_list|(
name|byte
operator|.
name|class
argument_list|,
literal|"toByteExact"
argument_list|,
name|long
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|TOBYTEWOOVERFLOW_FLOAT
specifier|final
specifier|static
name|Method
name|TOBYTEWOOVERFLOW_FLOAT
init|=
name|getAsmMethod
argument_list|(
name|byte
operator|.
name|class
argument_list|,
literal|"toByteWithoutOverflow"
argument_list|,
name|float
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|TOBYTEWOOVERFLOW_DOUBLE
specifier|final
specifier|static
name|Method
name|TOBYTEWOOVERFLOW_DOUBLE
init|=
name|getAsmMethod
argument_list|(
name|byte
operator|.
name|class
argument_list|,
literal|"toByteWithoutOverflow"
argument_list|,
name|double
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|TOSHORTEXACT_INT
specifier|final
specifier|static
name|Method
name|TOSHORTEXACT_INT
init|=
name|getAsmMethod
argument_list|(
name|short
operator|.
name|class
argument_list|,
literal|"toShortExact"
argument_list|,
name|int
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|TOSHORTEXACT_LONG
specifier|final
specifier|static
name|Method
name|TOSHORTEXACT_LONG
init|=
name|getAsmMethod
argument_list|(
name|short
operator|.
name|class
argument_list|,
literal|"toShortExact"
argument_list|,
name|long
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|TOSHORTWOOVERFLOW_FLOAT
specifier|final
specifier|static
name|Method
name|TOSHORTWOOVERFLOW_FLOAT
init|=
name|getAsmMethod
argument_list|(
name|short
operator|.
name|class
argument_list|,
literal|"toShortWithoutOverflow"
argument_list|,
name|float
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|TOSHORTWOOVERFLOW_DOUBLE
specifier|final
specifier|static
name|Method
name|TOSHORTWOOVERFLOW_DOUBLE
init|=
name|getAsmMethod
argument_list|(
name|short
operator|.
name|class
argument_list|,
literal|"toShortWihtoutOverflow"
argument_list|,
name|double
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|TOCHAREXACT_INT
specifier|final
specifier|static
name|Method
name|TOCHAREXACT_INT
init|=
name|getAsmMethod
argument_list|(
name|char
operator|.
name|class
argument_list|,
literal|"toCharExact"
argument_list|,
name|int
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|TOCHAREXACT_LONG
specifier|final
specifier|static
name|Method
name|TOCHAREXACT_LONG
init|=
name|getAsmMethod
argument_list|(
name|char
operator|.
name|class
argument_list|,
literal|"toCharExact"
argument_list|,
name|long
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|TOCHARWOOVERFLOW_FLOAT
specifier|final
specifier|static
name|Method
name|TOCHARWOOVERFLOW_FLOAT
init|=
name|getAsmMethod
argument_list|(
name|char
operator|.
name|class
argument_list|,
literal|"toCharWithoutOverflow"
argument_list|,
name|float
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|TOCHARWOOVERFLOW_DOUBLE
specifier|final
specifier|static
name|Method
name|TOCHARWOOVERFLOW_DOUBLE
init|=
name|getAsmMethod
argument_list|(
name|char
operator|.
name|class
argument_list|,
literal|"toCharWithoutOverflow"
argument_list|,
name|double
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|TOINTWOOVERFLOW_FLOAT
specifier|final
specifier|static
name|Method
name|TOINTWOOVERFLOW_FLOAT
init|=
name|getAsmMethod
argument_list|(
name|int
operator|.
name|class
argument_list|,
literal|"toIntWithoutOverflow"
argument_list|,
name|float
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|TOINTWOOVERFLOW_DOUBLE
specifier|final
specifier|static
name|Method
name|TOINTWOOVERFLOW_DOUBLE
init|=
name|getAsmMethod
argument_list|(
name|int
operator|.
name|class
argument_list|,
literal|"toIntWithoutOverflow"
argument_list|,
name|double
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|TOLONGWOOVERFLOW_FLOAT
specifier|final
specifier|static
name|Method
name|TOLONGWOOVERFLOW_FLOAT
init|=
name|getAsmMethod
argument_list|(
name|long
operator|.
name|class
argument_list|,
literal|"toLongWithoutOverflow"
argument_list|,
name|float
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|TOLONGWOOVERFLOW_DOUBLE
specifier|final
specifier|static
name|Method
name|TOLONGWOOVERFLOW_DOUBLE
init|=
name|getAsmMethod
argument_list|(
name|long
operator|.
name|class
argument_list|,
literal|"toLongWithoutOverflow"
argument_list|,
name|double
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|TOFLOATWOOVERFLOW_DOUBLE
specifier|final
specifier|static
name|Method
name|TOFLOATWOOVERFLOW_DOUBLE
init|=
name|getAsmMethod
argument_list|(
name|float
operator|.
name|class
argument_list|,
literal|"toFloatWihtoutOverflow"
argument_list|,
name|double
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|MULWOOVERLOW_FLOAT
specifier|final
specifier|static
name|Method
name|MULWOOVERLOW_FLOAT
init|=
name|getAsmMethod
argument_list|(
name|float
operator|.
name|class
argument_list|,
literal|"multiplyWithoutOverflow"
argument_list|,
name|float
operator|.
name|class
argument_list|,
name|float
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|MULWOOVERLOW_DOUBLE
specifier|final
specifier|static
name|Method
name|MULWOOVERLOW_DOUBLE
init|=
name|getAsmMethod
argument_list|(
name|double
operator|.
name|class
argument_list|,
literal|"multiplyWithoutOverflow"
argument_list|,
name|double
operator|.
name|class
argument_list|,
name|double
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DIVWOOVERLOW_INT
specifier|final
specifier|static
name|Method
name|DIVWOOVERLOW_INT
init|=
name|getAsmMethod
argument_list|(
name|int
operator|.
name|class
argument_list|,
literal|"divideWithoutOverflow"
argument_list|,
name|int
operator|.
name|class
argument_list|,
name|int
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DIVWOOVERLOW_LONG
specifier|final
specifier|static
name|Method
name|DIVWOOVERLOW_LONG
init|=
name|getAsmMethod
argument_list|(
name|long
operator|.
name|class
argument_list|,
literal|"divideWithoutOverflow"
argument_list|,
name|long
operator|.
name|class
argument_list|,
name|long
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DIVWOOVERLOW_FLOAT
specifier|final
specifier|static
name|Method
name|DIVWOOVERLOW_FLOAT
init|=
name|getAsmMethod
argument_list|(
name|float
operator|.
name|class
argument_list|,
literal|"divideWithoutOverflow"
argument_list|,
name|float
operator|.
name|class
argument_list|,
name|float
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DIVWOOVERLOW_DOUBLE
specifier|final
specifier|static
name|Method
name|DIVWOOVERLOW_DOUBLE
init|=
name|getAsmMethod
argument_list|(
name|double
operator|.
name|class
argument_list|,
literal|"divideWithoutOverflow"
argument_list|,
name|double
operator|.
name|class
argument_list|,
name|double
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|REMWOOVERLOW_FLOAT
specifier|final
specifier|static
name|Method
name|REMWOOVERLOW_FLOAT
init|=
name|getAsmMethod
argument_list|(
name|float
operator|.
name|class
argument_list|,
literal|"remainderWithoutOverflow"
argument_list|,
name|float
operator|.
name|class
argument_list|,
name|float
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|REMWOOVERLOW_DOUBLE
specifier|final
specifier|static
name|Method
name|REMWOOVERLOW_DOUBLE
init|=
name|getAsmMethod
argument_list|(
name|double
operator|.
name|class
argument_list|,
literal|"remainderWithoutOverflow"
argument_list|,
name|double
operator|.
name|class
argument_list|,
name|double
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|ADDWOOVERLOW_FLOAT
specifier|final
specifier|static
name|Method
name|ADDWOOVERLOW_FLOAT
init|=
name|getAsmMethod
argument_list|(
name|float
operator|.
name|class
argument_list|,
literal|"addWithoutOverflow"
argument_list|,
name|float
operator|.
name|class
argument_list|,
name|float
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|ADDWOOVERLOW_DOUBLE
specifier|final
specifier|static
name|Method
name|ADDWOOVERLOW_DOUBLE
init|=
name|getAsmMethod
argument_list|(
name|double
operator|.
name|class
argument_list|,
literal|"addWithoutOverflow"
argument_list|,
name|double
operator|.
name|class
argument_list|,
name|double
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|SUBWOOVERLOW_FLOAT
specifier|final
specifier|static
name|Method
name|SUBWOOVERLOW_FLOAT
init|=
name|getAsmMethod
argument_list|(
name|float
operator|.
name|class
argument_list|,
literal|"subtractWithoutOverflow"
argument_list|,
name|float
operator|.
name|class
argument_list|,
name|float
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|SUBWOOVERLOW_DOUBLE
specifier|final
specifier|static
name|Method
name|SUBWOOVERLOW_DOUBLE
init|=
name|getAsmMethod
argument_list|(
name|double
operator|.
name|class
argument_list|,
literal|"subtractWithoutOverflow"
argument_list|,
name|double
operator|.
name|class
argument_list|,
name|double
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|method|getAsmMethod
specifier|private
specifier|static
name|Method
name|getAsmMethod
parameter_list|(
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
name|rtype
parameter_list|,
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
modifier|...
name|ptypes
parameter_list|)
block|{
return|return
operator|new
name|Method
argument_list|(
name|name
argument_list|,
name|MethodType
operator|.
name|methodType
argument_list|(
name|rtype
argument_list|,
name|ptypes
argument_list|)
operator|.
name|toMethodDescriptorString
argument_list|()
argument_list|)
return|;
block|}
DECL|method|WriterConstants
specifier|private
name|WriterConstants
parameter_list|()
block|{}
block|}
end_class

end_unit

