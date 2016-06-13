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
name|LambdaMetafactory
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
name|MethodHandle
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
name|BitSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_comment
comment|/**  * General pool of constants used during the writing phase of compilation.  */
end_comment

begin_class
DECL|class|WriterConstants
specifier|public
specifier|final
class|class
name|WriterConstants
block|{
DECL|field|BASE_CLASS_NAME
specifier|public
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
DECL|field|BASE_CLASS_TYPE
specifier|public
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
DECL|field|CLASS_NAME
specifier|public
specifier|final
specifier|static
name|String
name|CLASS_NAME
init|=
name|BASE_CLASS_NAME
operator|+
literal|"$Script"
decl_stmt|;
DECL|field|CLASS_TYPE
specifier|public
specifier|final
specifier|static
name|Type
name|CLASS_TYPE
init|=
name|Type
operator|.
name|getObjectType
argument_list|(
name|CLASS_NAME
operator|.
name|replace
argument_list|(
literal|'.'
argument_list|,
literal|'/'
argument_list|)
argument_list|)
decl_stmt|;
DECL|field|CONSTRUCTOR
specifier|public
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
name|String
operator|.
name|class
argument_list|,
name|String
operator|.
name|class
argument_list|,
name|BitSet
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|CLINIT
specifier|public
specifier|final
specifier|static
name|Method
name|CLINIT
init|=
name|getAsmMethod
argument_list|(
name|void
operator|.
name|class
argument_list|,
literal|"<clinit>"
argument_list|)
decl_stmt|;
DECL|field|EXECUTE
specifier|public
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
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|PAINLESS_ERROR_TYPE
specifier|public
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
DECL|field|NEEDS_SCORE_TYPE
specifier|public
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
DECL|field|SCORER_TYPE
specifier|public
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
specifier|public
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
specifier|public
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
specifier|public
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
DECL|field|ITERATOR_TYPE
specifier|public
specifier|final
specifier|static
name|Type
name|ITERATOR_TYPE
init|=
name|Type
operator|.
name|getType
argument_list|(
name|Iterator
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|ITERATOR_HASNEXT
specifier|public
specifier|final
specifier|static
name|Method
name|ITERATOR_HASNEXT
init|=
name|getAsmMethod
argument_list|(
name|boolean
operator|.
name|class
argument_list|,
literal|"hasNext"
argument_list|)
decl_stmt|;
DECL|field|ITERATOR_NEXT
specifier|public
specifier|final
specifier|static
name|Method
name|ITERATOR_NEXT
init|=
name|getAsmMethod
argument_list|(
name|Object
operator|.
name|class
argument_list|,
literal|"next"
argument_list|)
decl_stmt|;
DECL|field|UTILITY_TYPE
specifier|public
specifier|final
specifier|static
name|Type
name|UTILITY_TYPE
init|=
name|Type
operator|.
name|getType
argument_list|(
name|Utility
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|STRING_TO_CHAR
specifier|public
specifier|final
specifier|static
name|Method
name|STRING_TO_CHAR
init|=
name|getAsmMethod
argument_list|(
name|char
operator|.
name|class
argument_list|,
literal|"StringTochar"
argument_list|,
name|String
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|CHAR_TO_STRING
specifier|public
specifier|final
specifier|static
name|Method
name|CHAR_TO_STRING
init|=
name|getAsmMethod
argument_list|(
name|String
operator|.
name|class
argument_list|,
literal|"charToString"
argument_list|,
name|char
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|METHOD_HANDLE_TYPE
specifier|public
specifier|final
specifier|static
name|Type
name|METHOD_HANDLE_TYPE
init|=
name|Type
operator|.
name|getType
argument_list|(
name|MethodHandle
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**      * A Method instance for {@linkplain Pattern#compile}. This isn't looked up from Definition because we intentionally don't add it there      * so that the script can't create regexes without this syntax. Essentially, our static regex syntax has a monopoly on building regexes      * because it can do it statically. This is both faster and prevents the script from doing something super slow like building a regex      * per time it is run.      */
DECL|field|PATTERN_COMPILE
specifier|public
specifier|final
specifier|static
name|Method
name|PATTERN_COMPILE
init|=
name|getAsmMethod
argument_list|(
name|Pattern
operator|.
name|class
argument_list|,
literal|"compile"
argument_list|,
name|String
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** dynamic callsite bootstrap signature */
DECL|field|DEF_BOOTSTRAP_TYPE
specifier|public
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
argument_list|,
name|Object
index|[]
operator|.
expr|class
argument_list|)
decl_stmt|;
DECL|field|DEF_BOOTSTRAP_HANDLE
specifier|public
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
name|DefBootstrap
operator|.
name|class
argument_list|)
argument_list|,
literal|"bootstrap"
argument_list|,
name|DEF_BOOTSTRAP_TYPE
operator|.
name|toMethodDescriptorString
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
DECL|field|DEF_UTIL_TYPE
specifier|public
specifier|final
specifier|static
name|Type
name|DEF_UTIL_TYPE
init|=
name|Type
operator|.
name|getType
argument_list|(
name|Def
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_TO_BOOLEAN
specifier|public
specifier|final
specifier|static
name|Method
name|DEF_TO_BOOLEAN
init|=
name|getAsmMethod
argument_list|(
name|boolean
operator|.
name|class
argument_list|,
literal|"DefToboolean"
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_TO_BYTE_IMPLICIT
specifier|public
specifier|final
specifier|static
name|Method
name|DEF_TO_BYTE_IMPLICIT
init|=
name|getAsmMethod
argument_list|(
name|byte
operator|.
name|class
argument_list|,
literal|"DefTobyteImplicit"
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_TO_SHORT_IMPLICIT
specifier|public
specifier|final
specifier|static
name|Method
name|DEF_TO_SHORT_IMPLICIT
init|=
name|getAsmMethod
argument_list|(
name|short
operator|.
name|class
argument_list|,
literal|"DefToshortImplicit"
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_TO_CHAR_IMPLICIT
specifier|public
specifier|final
specifier|static
name|Method
name|DEF_TO_CHAR_IMPLICIT
init|=
name|getAsmMethod
argument_list|(
name|char
operator|.
name|class
argument_list|,
literal|"DefTocharImplicit"
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_TO_INT_IMPLICIT
specifier|public
specifier|final
specifier|static
name|Method
name|DEF_TO_INT_IMPLICIT
init|=
name|getAsmMethod
argument_list|(
name|int
operator|.
name|class
argument_list|,
literal|"DefTointImplicit"
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_TO_LONG_IMPLICIT
specifier|public
specifier|final
specifier|static
name|Method
name|DEF_TO_LONG_IMPLICIT
init|=
name|getAsmMethod
argument_list|(
name|long
operator|.
name|class
argument_list|,
literal|"DefTolongImplicit"
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_TO_FLOAT_IMPLICIT
specifier|public
specifier|final
specifier|static
name|Method
name|DEF_TO_FLOAT_IMPLICIT
init|=
name|getAsmMethod
argument_list|(
name|float
operator|.
name|class
argument_list|,
literal|"DefTofloatImplicit"
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_TO_DOUBLE_IMPLICIT
specifier|public
specifier|final
specifier|static
name|Method
name|DEF_TO_DOUBLE_IMPLICIT
init|=
name|getAsmMethod
argument_list|(
name|double
operator|.
name|class
argument_list|,
literal|"DefTodoubleImplicit"
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_TO_BYTE_EXPLICIT
specifier|public
specifier|final
specifier|static
name|Method
name|DEF_TO_BYTE_EXPLICIT
init|=
name|getAsmMethod
argument_list|(
name|byte
operator|.
name|class
argument_list|,
literal|"DefTobyteExplicit"
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_TO_SHORT_EXPLICIT
specifier|public
specifier|final
specifier|static
name|Method
name|DEF_TO_SHORT_EXPLICIT
init|=
name|getAsmMethod
argument_list|(
name|short
operator|.
name|class
argument_list|,
literal|"DefToshortExplicit"
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_TO_CHAR_EXPLICIT
specifier|public
specifier|final
specifier|static
name|Method
name|DEF_TO_CHAR_EXPLICIT
init|=
name|getAsmMethod
argument_list|(
name|char
operator|.
name|class
argument_list|,
literal|"DefTocharExplicit"
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_TO_INT_EXPLICIT
specifier|public
specifier|final
specifier|static
name|Method
name|DEF_TO_INT_EXPLICIT
init|=
name|getAsmMethod
argument_list|(
name|int
operator|.
name|class
argument_list|,
literal|"DefTointExplicit"
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_TO_LONG_EXPLICIT
specifier|public
specifier|final
specifier|static
name|Method
name|DEF_TO_LONG_EXPLICIT
init|=
name|getAsmMethod
argument_list|(
name|long
operator|.
name|class
argument_list|,
literal|"DefTolongExplicit"
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_TO_FLOAT_EXPLICIT
specifier|public
specifier|final
specifier|static
name|Method
name|DEF_TO_FLOAT_EXPLICIT
init|=
name|getAsmMethod
argument_list|(
name|float
operator|.
name|class
argument_list|,
literal|"DefTofloatExplicit"
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_TO_DOUBLE_EXPLICIT
specifier|public
specifier|final
specifier|static
name|Method
name|DEF_TO_DOUBLE_EXPLICIT
init|=
name|getAsmMethod
argument_list|(
name|double
operator|.
name|class
argument_list|,
literal|"DefTodoubleExplicit"
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_NOT_CALL
specifier|public
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
specifier|public
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
specifier|public
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
specifier|public
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
specifier|public
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
specifier|public
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
specifier|public
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
specifier|public
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
name|int
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_RSH_CALL
specifier|public
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
name|int
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_USH_CALL
specifier|public
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
name|int
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|DEF_AND_CALL
specifier|public
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
specifier|public
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
specifier|public
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
specifier|public
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
specifier|public
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
specifier|public
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
specifier|public
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
specifier|public
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
comment|/** invokedynamic bootstrap for lambda expression/method references */
DECL|field|LAMBDA_BOOTSTRAP_TYPE
specifier|public
specifier|final
specifier|static
name|MethodType
name|LAMBDA_BOOTSTRAP_TYPE
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
name|Object
index|[]
operator|.
expr|class
argument_list|)
decl_stmt|;
DECL|field|LAMBDA_BOOTSTRAP_HANDLE
specifier|public
specifier|final
specifier|static
name|Handle
name|LAMBDA_BOOTSTRAP_HANDLE
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
name|LambdaMetafactory
operator|.
name|class
argument_list|)
argument_list|,
literal|"altMetafactory"
argument_list|,
name|LAMBDA_BOOTSTRAP_TYPE
operator|.
name|toMethodDescriptorString
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
comment|/** dynamic invokedynamic bootstrap for indy string concats (Java 9+) */
DECL|field|INDY_STRING_CONCAT_BOOTSTRAP_HANDLE
specifier|public
specifier|final
specifier|static
name|Handle
name|INDY_STRING_CONCAT_BOOTSTRAP_HANDLE
decl_stmt|;
static|static
block|{
name|Handle
name|bs
decl_stmt|;
try|try
block|{
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
name|factory
init|=
name|Class
operator|.
name|forName
argument_list|(
literal|"java.lang.invoke.StringConcatFactory"
argument_list|)
decl_stmt|;
specifier|final
name|String
name|methodName
init|=
literal|"makeConcat"
decl_stmt|;
specifier|final
name|MethodType
name|type
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
argument_list|)
decl_stmt|;
comment|// ensure it is there:
name|MethodHandles
operator|.
name|publicLookup
argument_list|()
operator|.
name|findStatic
argument_list|(
name|factory
argument_list|,
name|methodName
argument_list|,
name|type
argument_list|)
expr_stmt|;
name|bs
operator|=
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
name|factory
argument_list|)
argument_list|,
name|methodName
argument_list|,
name|type
operator|.
name|toMethodDescriptorString
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ReflectiveOperationException
name|e
parameter_list|)
block|{
comment|// not Java 9 - we set it null, so MethodWriter uses StringBuilder:
name|bs
operator|=
literal|null
expr_stmt|;
block|}
name|INDY_STRING_CONCAT_BOOTSTRAP_HANDLE
operator|=
name|bs
expr_stmt|;
block|}
DECL|field|MAX_INDY_STRING_CONCAT_ARGS
specifier|public
specifier|final
specifier|static
name|int
name|MAX_INDY_STRING_CONCAT_ARGS
init|=
literal|200
decl_stmt|;
DECL|field|STRING_TYPE
specifier|public
specifier|final
specifier|static
name|Type
name|STRING_TYPE
init|=
name|Type
operator|.
name|getType
argument_list|(
name|String
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|STRINGBUILDER_TYPE
specifier|public
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
specifier|public
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
specifier|public
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
specifier|public
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
specifier|public
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
specifier|public
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
specifier|public
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
specifier|public
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
specifier|public
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
specifier|public
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
specifier|public
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
DECL|field|CHECKEQUALS
specifier|public
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

