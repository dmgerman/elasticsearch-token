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
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|CharBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|PatternSyntaxException
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|singletonMap
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|containsString
import|;
end_import

begin_class
DECL|class|RegexTests
specifier|public
class|class
name|RegexTests
extends|extends
name|ScriptTestCase
block|{
annotation|@
name|Override
DECL|method|scriptEngineSettings
specifier|protected
name|Settings
name|scriptEngineSettings
parameter_list|()
block|{
comment|// Enable regexes just for this test. They are disabled by default.
return|return
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|CompilerSettings
operator|.
name|REGEX_ENABLED
operator|.
name|getKey
argument_list|()
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|testPatternAfterReturn
specifier|public
name|void
name|testPatternAfterReturn
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"return 'foo' ==~ /foo/"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|exec
argument_list|(
literal|"return 'bar' ==~ /foo/"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testBackslashEscapesForwardSlash
specifier|public
name|void
name|testBackslashEscapesForwardSlash
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"'//' ==~ /\\/\\//"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testBackslashEscapeBackslash
specifier|public
name|void
name|testBackslashEscapeBackslash
parameter_list|()
block|{
comment|// Both of these are single backslashes but java escaping + Painless escaping....
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"'\\\\' ==~ /\\\\/"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRegexIsNonGreedy
specifier|public
name|void
name|testRegexIsNonGreedy
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"def s = /\\\\/.split('.\\\\.'); return s[1] ==~ /\\./"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testPatternAfterAssignment
specifier|public
name|void
name|testPatternAfterAssignment
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"def a = /foo/; return 'foo' ==~ a"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testPatternInIfStement
specifier|public
name|void
name|testPatternInIfStement
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"if (/foo/.matcher('foo').matches()) { return true } else { return false }"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"if ('foo' ==~ /foo/) { return true } else { return false }"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testPatternAfterInfixBoolean
specifier|public
name|void
name|testPatternAfterInfixBoolean
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"return false || /foo/.matcher('foo').matches()"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"return true&& /foo/.matcher('foo').matches()"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"return false || 'foo' ==~ /foo/"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"return true&& 'foo' ==~ /foo/"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testPatternAfterUnaryNotBoolean
specifier|public
name|void
name|testPatternAfterUnaryNotBoolean
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|exec
argument_list|(
literal|"return !/foo/.matcher('foo').matches()"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"return !/foo/.matcher('bar').matches()"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testInTernaryCondition
specifier|public
name|void
name|testInTernaryCondition
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"return /foo/.matcher('foo').matches() ? true : false"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|exec
argument_list|(
literal|"def i = 0; i += /foo/.matcher('foo').matches() ? 1 : 1; return i"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"return 'foo' ==~ /foo/ ? true : false"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|exec
argument_list|(
literal|"def i = 0; i += 'foo' ==~ /foo/ ? 1 : 1; return i"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testInTernaryTrueArm
specifier|public
name|void
name|testInTernaryTrueArm
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"def i = true; return i ? /foo/.matcher('foo').matches() : false"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"def i = true; return i ? 'foo' ==~ /foo/ : false"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testInTernaryFalseArm
specifier|public
name|void
name|testInTernaryFalseArm
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"def i = false; return i ? false : 'foo' ==~ /foo/"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRegexInFunction
specifier|public
name|void
name|testRegexInFunction
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"boolean m(String s) {/foo/.matcher(s).matches()} m('foo')"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"boolean m(String s) {s ==~ /foo/} m('foo')"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testReturnRegexFromFunction
specifier|public
name|void
name|testReturnRegexFromFunction
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"Pattern m(boolean a) {a ? /foo/ : /bar/} m(true).matcher('foo').matches()"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"Pattern m(boolean a) {a ? /foo/ : /bar/} 'foo' ==~ m(true)"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|exec
argument_list|(
literal|"Pattern m(boolean a) {a ? /foo/ : /bar/} m(false).matcher('foo').matches()"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|exec
argument_list|(
literal|"Pattern m(boolean a) {a ? /foo/ : /bar/} 'foo' ==~ m(false)"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCallMatcherDirectly
specifier|public
name|void
name|testCallMatcherDirectly
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"return /foo/.matcher('foo').matches()"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|exec
argument_list|(
literal|"return /foo/.matcher('bar').matches()"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testFindInIf
specifier|public
name|void
name|testFindInIf
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"if ('fooasdfbasdf' =~ /foo/) {return true} else {return false}"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"if ('1fooasdfbasdf' =~ /foo/) {return true} else {return false}"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|exec
argument_list|(
literal|"if ('1f11ooasdfbasdf' =~ /foo/) {return true} else {return false}"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testFindCastToBoolean
specifier|public
name|void
name|testFindCastToBoolean
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"return (boolean)('fooasdfbasdf' =~ /foo/)"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"return (boolean)('111fooasdfbasdf' =~ /foo/)"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|exec
argument_list|(
literal|"return (boolean)('fo11oasdfbasdf' =~ /foo/)"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testFindOrStringConcat
specifier|public
name|void
name|testFindOrStringConcat
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"return 'f' + 'o' + 'o' =~ /foo/"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testFindOfDef
specifier|public
name|void
name|testFindOfDef
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"def s = 'foo'; return s =~ /foo/"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testFindOnInput
specifier|public
name|void
name|testFindOnInput
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|exec
argument_list|(
literal|"return params.s =~ /foo/"
argument_list|,
name|singletonMap
argument_list|(
literal|"s"
argument_list|,
literal|"fooasdfdf"
argument_list|)
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|exec
argument_list|(
literal|"return params.s =~ /foo/"
argument_list|,
name|singletonMap
argument_list|(
literal|"s"
argument_list|,
literal|"11f2ooasdfdf"
argument_list|)
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testGroup
specifier|public
name|void
name|testGroup
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|"foo"
argument_list|,
name|exec
argument_list|(
literal|"Matcher m = /foo/.matcher('foo'); m.find(); return m.group()"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNumberedGroup
specifier|public
name|void
name|testNumberedGroup
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|"o"
argument_list|,
name|exec
argument_list|(
literal|"Matcher m = /(f)(o)o/.matcher('foo'); m.find(); return m.group(2)"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNamedGroup
specifier|public
name|void
name|testNamedGroup
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|"o"
argument_list|,
name|exec
argument_list|(
literal|"Matcher m = /(?<first>f)(?<second>o)o/.matcher('foo'); m.find(); return m.namedGroup('second')"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Make sure some methods on Pattern are whitelisted
DECL|method|testSplit
specifier|public
name|void
name|testSplit
parameter_list|()
block|{
name|assertArrayEquals
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"cat"
block|,
literal|"dog"
block|}
argument_list|,
operator|(
name|String
index|[]
operator|)
name|exec
argument_list|(
literal|"/,/.split('cat,dog')"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSplitAsStream
specifier|public
name|void
name|testSplitAsStream
parameter_list|()
block|{
name|assertEquals
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"cat"
argument_list|,
literal|"dog"
argument_list|)
argument_list|)
argument_list|,
name|exec
argument_list|(
literal|"/,/.splitAsStream('cat,dog').collect(Collectors.toSet())"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Make sure the flags are set
DECL|method|testMultilineFlag
specifier|public
name|void
name|testMultilineFlag
parameter_list|()
block|{
name|assertEquals
argument_list|(
name|Pattern
operator|.
name|MULTILINE
argument_list|,
name|exec
argument_list|(
literal|"/./m.flags()"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSinglelineFlag
specifier|public
name|void
name|testSinglelineFlag
parameter_list|()
block|{
name|assertEquals
argument_list|(
name|Pattern
operator|.
name|DOTALL
argument_list|,
name|exec
argument_list|(
literal|"/./s.flags()"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testInsensitiveFlag
specifier|public
name|void
name|testInsensitiveFlag
parameter_list|()
block|{
name|assertEquals
argument_list|(
name|Pattern
operator|.
name|CASE_INSENSITIVE
argument_list|,
name|exec
argument_list|(
literal|"/./i.flags()"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testExtendedFlag
specifier|public
name|void
name|testExtendedFlag
parameter_list|()
block|{
name|assertEquals
argument_list|(
name|Pattern
operator|.
name|COMMENTS
argument_list|,
name|exec
argument_list|(
literal|"/./x.flags()"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testUnicodeCaseFlag
specifier|public
name|void
name|testUnicodeCaseFlag
parameter_list|()
block|{
name|assertEquals
argument_list|(
name|Pattern
operator|.
name|UNICODE_CASE
argument_list|,
name|exec
argument_list|(
literal|"/./u.flags()"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testUnicodeCharacterClassFlag
specifier|public
name|void
name|testUnicodeCharacterClassFlag
parameter_list|()
block|{
name|assertEquals
argument_list|(
name|Pattern
operator|.
name|UNICODE_CASE
operator||
name|Pattern
operator|.
name|UNICODE_CHARACTER_CLASS
argument_list|,
name|exec
argument_list|(
literal|"/./U.flags()"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testLiteralFlag
specifier|public
name|void
name|testLiteralFlag
parameter_list|()
block|{
name|assertEquals
argument_list|(
name|Pattern
operator|.
name|LITERAL
argument_list|,
name|exec
argument_list|(
literal|"/./l.flags()"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCanonicalEquivalenceFlag
specifier|public
name|void
name|testCanonicalEquivalenceFlag
parameter_list|()
block|{
name|assertEquals
argument_list|(
name|Pattern
operator|.
name|CANON_EQ
argument_list|,
name|exec
argument_list|(
literal|"/./c.flags()"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testManyFlags
specifier|public
name|void
name|testManyFlags
parameter_list|()
block|{
name|assertEquals
argument_list|(
name|Pattern
operator|.
name|CANON_EQ
operator||
name|Pattern
operator|.
name|CASE_INSENSITIVE
operator||
name|Pattern
operator|.
name|UNICODE_CASE
operator||
name|Pattern
operator|.
name|COMMENTS
argument_list|,
name|exec
argument_list|(
literal|"/./ciux.flags()"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testReplaceAllMatchesString
specifier|public
name|void
name|testReplaceAllMatchesString
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|"thE qUIck brOwn fOx"
argument_list|,
name|exec
argument_list|(
literal|"'the quick brown fox'.replaceAll(/[aeiou]/, m -> m.group().toUpperCase(Locale.ROOT))"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testReplaceAllMatchesCharSequence
specifier|public
name|void
name|testReplaceAllMatchesCharSequence
parameter_list|()
block|{
name|CharSequence
name|charSequence
init|=
name|CharBuffer
operator|.
name|wrap
argument_list|(
literal|"the quick brown fox"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"thE qUIck brOwn fOx"
argument_list|,
name|exec
argument_list|(
literal|"params.a.replaceAll(/[aeiou]/, m -> m.group().toUpperCase(Locale.ROOT))"
argument_list|,
name|singletonMap
argument_list|(
literal|"a"
argument_list|,
name|charSequence
argument_list|)
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testReplaceAllNoMatchString
specifier|public
name|void
name|testReplaceAllNoMatchString
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|"i am cat"
argument_list|,
name|exec
argument_list|(
literal|"'i am cat'.replaceAll(/dolphin/, m -> m.group().toUpperCase(Locale.ROOT))"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testReplaceAllNoMatchCharSequence
specifier|public
name|void
name|testReplaceAllNoMatchCharSequence
parameter_list|()
block|{
name|CharSequence
name|charSequence
init|=
name|CharBuffer
operator|.
name|wrap
argument_list|(
literal|"i am cat"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"i am cat"
argument_list|,
name|exec
argument_list|(
literal|"params.a.replaceAll(/dolphin/, m -> m.group().toUpperCase(Locale.ROOT))"
argument_list|,
name|singletonMap
argument_list|(
literal|"a"
argument_list|,
name|charSequence
argument_list|)
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testReplaceAllQuoteReplacement
specifier|public
name|void
name|testReplaceAllQuoteReplacement
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|"th/E q/U/Ick br/Own f/Ox"
argument_list|,
name|exec
argument_list|(
literal|"'the quick brown fox'.replaceAll(/[aeiou]/, m -> '/' + m.group().toUpperCase(Locale.ROOT))"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"th$E q$U$Ick br$Own f$Ox"
argument_list|,
name|exec
argument_list|(
literal|"'the quick brown fox'.replaceAll(/[aeiou]/, m -> '$' + m.group().toUpperCase(Locale.ROOT))"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testReplaceFirstMatchesString
specifier|public
name|void
name|testReplaceFirstMatchesString
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|"thE quick brown fox"
argument_list|,
name|exec
argument_list|(
literal|"'the quick brown fox'.replaceFirst(/[aeiou]/, m -> m.group().toUpperCase(Locale.ROOT))"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testReplaceFirstMatchesCharSequence
specifier|public
name|void
name|testReplaceFirstMatchesCharSequence
parameter_list|()
block|{
name|CharSequence
name|charSequence
init|=
name|CharBuffer
operator|.
name|wrap
argument_list|(
literal|"the quick brown fox"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"thE quick brown fox"
argument_list|,
name|exec
argument_list|(
literal|"params.a.replaceFirst(/[aeiou]/, m -> m.group().toUpperCase(Locale.ROOT))"
argument_list|,
name|singletonMap
argument_list|(
literal|"a"
argument_list|,
name|charSequence
argument_list|)
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testReplaceFirstNoMatchString
specifier|public
name|void
name|testReplaceFirstNoMatchString
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|"i am cat"
argument_list|,
name|exec
argument_list|(
literal|"'i am cat'.replaceFirst(/dolphin/, m -> m.group().toUpperCase(Locale.ROOT))"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testReplaceFirstNoMatchCharSequence
specifier|public
name|void
name|testReplaceFirstNoMatchCharSequence
parameter_list|()
block|{
name|CharSequence
name|charSequence
init|=
name|CharBuffer
operator|.
name|wrap
argument_list|(
literal|"i am cat"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"i am cat"
argument_list|,
name|exec
argument_list|(
literal|"params.a.replaceFirst(/dolphin/, m -> m.group().toUpperCase(Locale.ROOT))"
argument_list|,
name|singletonMap
argument_list|(
literal|"a"
argument_list|,
name|charSequence
argument_list|)
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testReplaceFirstQuoteReplacement
specifier|public
name|void
name|testReplaceFirstQuoteReplacement
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|"th/E quick brown fox"
argument_list|,
name|exec
argument_list|(
literal|"'the quick brown fox'.replaceFirst(/[aeiou]/, m -> '/' + m.group().toUpperCase(Locale.ROOT))"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"th$E quick brown fox"
argument_list|,
name|exec
argument_list|(
literal|"'the quick brown fox'.replaceFirst(/[aeiou]/, m -> '$' + m.group().toUpperCase(Locale.ROOT))"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCantUsePatternCompile
specifier|public
name|void
name|testCantUsePatternCompile
parameter_list|()
block|{
name|IllegalArgumentException
name|e
init|=
name|expectScriptThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"Pattern.compile('aa')"
argument_list|)
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Unknown call [compile] with [1] arguments on type [Pattern]."
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testBadRegexPattern
specifier|public
name|void
name|testBadRegexPattern
parameter_list|()
block|{
name|ScriptException
name|e
init|=
name|expectThrows
argument_list|(
name|ScriptException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"/\\ujjjj/"
argument_list|)
expr_stmt|;
comment|// Invalid unicode
block|}
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Error compiling regex: Illegal Unicode escape sequence"
argument_list|,
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
comment|// And make sure the location of the error points to the offset inside the pattern
name|assertEquals
argument_list|(
literal|"/\\ujjjj/"
argument_list|,
name|e
operator|.
name|getScriptStack
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"   ^---- HERE"
argument_list|,
name|e
operator|.
name|getScriptStack
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRegexAgainstNumber
specifier|public
name|void
name|testRegexAgainstNumber
parameter_list|()
block|{
name|ClassCastException
name|e
init|=
name|expectScriptThrows
argument_list|(
name|ClassCastException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"12 ==~ /cat/"
argument_list|)
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Cannot cast from [int] to [String]."
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testBogusRegexFlag
specifier|public
name|void
name|testBogusRegexFlag
parameter_list|()
block|{
name|IllegalArgumentException
name|e
init|=
name|expectScriptThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"/asdf/b"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Not picky so we get a non-assertion error
block|}
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"unexpected token ['b'] was expecting one of [{<EOF>, ';'}]."
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

