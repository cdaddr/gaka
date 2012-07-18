# gaka 0.3.0
by [Brian Carper](http://briancarper.net)

Gaka is a CSS-generating library for Clojure inspired partly by
[Sass](http://sass-lang.com/) and similar to
[Hiccup](http://github.com/weavejester/hiccup).

## Features

* Simple
* Indented output
* Selector nesting
* "Mixins"

## Purpose

CSS syntax is verbose, with lots of curly braces and semi-colons.  Writing CSS
as s-expressions is a way to ensure you have proper syntax, because the program
handles the syntax for you.  And it lets you write CSS very quickly using an
editor that's good at manipulating s-expressions.

CSS has a lot of repetition in selectors.  `body #content div a {...}` etc.
You can remove most of this verbosity via nesting.  S-expressions are a great
way to express that nesting.  (Sass uses indentation for the same purpose.)

CSS rules in Gaka are just vectors of keywords and strings and numbers, which
means you can easily generate and manipulate them programatically.

## Example

Rules are vectors, where the first element is a selector and the rest are
either key/value pairs, or sub-rules.

    user> (require '(gaka [core :as gaka]))
    nil
    user> (def rules [:div#foo
                      :margin "0px"
                      [:span.bar
                       :color "black"
                       :font-weight "bold"
                       [:a:hover
                        :text-decoration "none"]]])
    #'user/rules
    user> (println (gaka/css rules))
    div#foo {
      margin: 0px;}

      div#foo span.bar {
        color: black;
        font-weight: bold;}

        div#foo span.bar a:hover {
          text-decoration: none;}


    nil
    user> (binding [gaka/*print-indent* false]
            (println (gaka/css rules)))
    div#foo {
    margin: 0px;}

    div#foo span.bar {
    color: black;
    font-weight: bold;}

    div#foo span.bar a:hover {
    text-decoration: none;}


    nil
    user> (gaka/save-css "foo.css" rules)
    nil

Anything in a seq (e.g. a list) will be flattened into the surrounding context,
which lets you have "mixins".

    user> (def standard-attrs (list :margin 0 :padding 0 :font-size "12px"))
    #'user/standard-attrs
    user> (println (gaka/css [:div standard-attrs :color "red"]))
    div {
      margin: 0;
      padding: 0;
      font-size: 12px;
      color: red;}
    user> (defn color [x] (list :color x))
    #'user/color
    user> (println (gaka/css [:div (color "red")]))
    div {
      color: red;}

You can also use maps for attributes.  Keep in mind that maps are unordered, whereas
order is significant in CSS (attributes can override earlier attributes).  To preserve
order, either use "flattened" key/value pairs, or a mixture of map and flattened
versions.

    ;; WRONG!  The order of map keys/values is unpredictable.
    ;; You may or may not get what you want.
    user> (println (gaka/css [:a {:border 0 :border-left "1px"}]))
    a {
      border-left: 1px;
      border: 0;}

    ;; OK
    user> (println (gaka/css [:a :border 0 :border-left "1px"]))
    a {
      border: 0;
      border-left: 1px;}

    ;; OK
    user> (println (gaka/css [:a (list :border 0 :border-left "1px")]))
    a {
      border: 0;
      border-left: 1px;}

    ;; OK
    user> (println (gaka/css [:a :border 0 {:border-left "1px"}]))
    a {
      border: 0;
      border-left: 1px;}

    ;; OK
    user> (println (gaka/css [:a {:border 0} {:border-left "1px"}]))
    a {
      border: 0;
      border-left: 1px;}

If you want a fancy selector or attribute that doesn't work as a keyword, use a
string.

    user> (println (gaka/css ["input[type=text]" :font-family "\"Bitstream Vera Sans\", monospace"]))
    input[type=text] {
      font-family: "Bitstream Vera Sans", monospace;}

If you want output suitable for inline CSS, use `inline-css`:

    user> (println (str "<div style=\""
                        (gaka/inline-css :color :red)
                        "\">foo</div>"))
    <div style="color: red;">foo</div>

An easy way to compile your CSS to a file and make sure it's always up-to-date
is to throw a `save-css` call at the bottom of your source file.

    (ns my-site.css
      (:require (gaka [core :as gaka)

    (def rules [...])

    (save-css "public/css/style.css" rules)

Now every time you re-compile this file (for example, `C-c C-k` in
Slime/Emacs), a static CSS file in `public/css` will be generated or updated.
This is the prefered way to serve CSS files for a web app (to avoid
re-compiling your CSS on every request, which is probably pointless).

That's about it.

## Limitations

Gaka currently outputs less-than-optimal CSS under certain circumstances and
errs on the side of verbosity to preserve correctness.

Gaka doesn't validate your CSS or check your spelling.

Gaka makes no attempt to be fast.  You should compile your CSS, save it to a
file and serve it statically.

I wrote (most of) this in one afternoon while eating a tasty ham and turky sandwich.
Bugs are likely.

## Install

To fetch from CLojars (via Leiningen) put this in your project.clj:

    [gaka "0.3.0"]

## License

Eclipse Public License 1.0, see http://opensource.org/licenses/eclipse-1.0.php.

## Thanks

[Steve Purcell](http://github.com/purcell) for map syntax and inline CSS.

